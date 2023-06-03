package kotbase

import cnames.structs.CBLReplicator
import kotbase.internal.fleece.keys
import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotbase.util.to
import kotbase.util.toList
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.staticCFunction
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class Replicator
private constructor(
    public val actual: CPointer<CBLReplicator>,
    private val immutableConfig: ImmutableReplicatorConfiguration
) {

    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLReplicator_Release(it)
    }

    public actual constructor(config: ReplicatorConfiguration) :
            this(ImmutableReplicatorConfiguration(config))

    private constructor(config: ImmutableReplicatorConfiguration) : this(
        wrapCBLError { error ->
            CBLReplicator_Create(config.actual, error)!!
        },
        config
    )

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(config)

    public actual fun start() {
        CBLReplicator_Start(actual, false)
    }

    public actual fun start(resetCheckpoint: Boolean) {
        CBLReplicator_Start(actual, resetCheckpoint)
    }

    public actual fun stop() {
        CBLReplicator_Stop(actual)
    }

    public actual val config: ReplicatorConfiguration
        get() = ReplicatorConfiguration(immutableConfig)

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(CBLReplicator_Status(actual))

    public actual val serverCertificates: List<ByteArray>?
        get() = null

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> {
        return wrapCBLError { error ->
            val dict = CBLReplicator_PendingDocumentIDs(actual, error)
            dict?.keys()?.toSet()?.also {
                FLDict_Release(dict)
            } ?: emptySet()
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean {
        return wrapCBLError { error ->
            CBLReplicator_IsDocumentPending(actual, docId.toFLString(), error)
        }
    }

    private val changeListeners = mutableListOf<StableRef<ReplicatorChangeListenerHolder>?>()

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        val holder = ReplicatorChangeListenerHolder(listener, this)
        val (index, stableRef) = addListener(changeListeners, holder)
        return DelegatedListenerToken(
            CBLReplicator_AddChangeListener(
                actual,
                staticCFunction { ref, _, status ->
                    with(ref.to<ReplicatorChangeListenerHolder>()) {
                        this.listener(ReplicatorChange(replicator, ReplicatorStatus(status!!)))
                    }
                },
                stableRef
            )!!,
            ListenerTokenType.REPLICATOR,
            index
        )
    }

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    private val documentChangeListeners =
        mutableListOf<StableRef<DocumentReplicationListenerHolder>?>()

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        val holder = DocumentReplicationListenerHolder(listener, this)
        val (index, stableRef) = addListener(documentChangeListeners, holder)
        return DelegatedListenerToken(
            CBLReplicator_AddDocumentReplicationListener(
                actual,
                staticCFunction { ref, _, isPush, numDocuments, documents ->
                    with(ref.to<DocumentReplicationListenerHolder>()) {
                        this.listener(
                            DocumentReplication(
                                replicator,
                                isPush,
                                documents!!.toList(numDocuments.toInt()) { ReplicatedDocument(it) }
                            )
                        )
                    }
                },
                stableRef
            )!!,
            ListenerTokenType.DOCUMENT_REPLICATION,
            index
        )
    }

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        val exists = when (token.type) {
            ListenerTokenType.REPLICATOR -> removeListener(changeListeners, token.index)
            ListenerTokenType.DOCUMENT_REPLICATION ->
                removeListener(documentChangeListeners, token.index)
            else -> error("${token.type} change listener can't be removed from Replicator instance")
        }
        if (exists) {
            CBLListener_Remove(token.actual)
        }
    }
}
