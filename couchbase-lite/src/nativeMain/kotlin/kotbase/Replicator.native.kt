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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import libcblite.*
import kotlin.coroutines.CoroutineContext
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class Replicator
private constructor(
    public val actual: CPointer<CBLReplicator>,
    private val immutableConfig: ImmutableReplicatorConfiguration
) {

    @OptIn(ExperimentalNativeApi::class)
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
        val holder = ReplicatorChangeDefaultListenerHolder(listener, this)
        return addNativeChangeListener(holder)
    }

    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: ReplicatorChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = ReplicatorChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        return SuspendListenerToken(scope, token)
    }

    public actual fun addChangeListener(scope: CoroutineScope, listener: ReplicatorChangeSuspendListener) {
        val holder = ReplicatorChangeSuspendListenerHolder(listener, this, scope)
        val token = addNativeChangeListener(holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            removeChangeListener(token)
        }
    }

    private fun addNativeChangeListener(holder: ReplicatorChangeListenerHolder): DelegatedListenerToken {
        val (index, stableRef) = addListener(changeListeners, holder)
        return DelegatedListenerToken(
            CBLReplicator_AddChangeListener(
                actual,
                nativeChangeListener(),
                stableRef
            )!!,
            ListenerTokenType.REPLICATOR,
            index
        )
    }

    private fun nativeChangeListener(): CBLReplicatorChangeListener {
        return staticCFunction { ref, _, status ->
            with(ref.to<ReplicatorChangeListenerHolder>()) {
                val change = ReplicatorChange(replicator, ReplicatorStatus(status!!))
                when (this) {
                    is ReplicatorChangeDefaultListenerHolder -> listener(change)
                    is ReplicatorChangeSuspendListenerHolder -> scope.launch {
                        listener(change)
                    }
                }
            }
        }
    }

    private val documentChangeListeners =
        mutableListOf<StableRef<DocumentReplicationListenerHolder>?>()

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        val holder = DocumentReplicationDefaultListenerHolder(listener, this)
        return addNativeDocumentReplicationListener(holder)
    }

    public actual fun addDocumentReplicationListener(
        context: CoroutineContext,
        listener: DocumentReplicationSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val holder = DocumentReplicationSuspendListenerHolder(listener, this, scope)
        val token = addNativeDocumentReplicationListener(holder)
        return SuspendListenerToken(scope, token)
    }

    public actual fun addDocumentReplicationListener(
        scope: CoroutineScope,
        listener: DocumentReplicationSuspendListener
    ) {
        val holder = DocumentReplicationSuspendListenerHolder(listener, this, scope)
        val token = addNativeDocumentReplicationListener(holder)
        scope.coroutineContext[Job]?.invokeOnCompletion {
            removeChangeListener(token)
        }
    }

    private fun addNativeDocumentReplicationListener(holder: DocumentReplicationListenerHolder): DelegatedListenerToken {
        val (index, stableRef) = addListener(documentChangeListeners, holder)
        return DelegatedListenerToken(
            CBLReplicator_AddDocumentReplicationListener(
                actual,
                nativeDocumentReplicationListener(),
                stableRef
            )!!,
            ListenerTokenType.DOCUMENT_REPLICATION,
            index
        )
    }

    private fun nativeDocumentReplicationListener(): CBLDocumentReplicationListener {
        return staticCFunction { ref, _, isPush, numDocuments, docs ->
            val documents = docs!!.toList(numDocuments.toInt()) { ReplicatedDocument(it) }
            with(ref.to<DocumentReplicationListenerHolder>()) {
                val replication = DocumentReplication(replicator, isPush, documents)
                when (this) {
                    is DocumentReplicationDefaultListenerHolder -> listener(replication)
                    is DocumentReplicationSuspendListenerHolder -> scope.launch {
                        listener(replication)
                    }
                }
            }
        }
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            removeChangeListener(token.token)
            token.scope.cancel()
        } else {
            removeChangeListener(token as DelegatedListenerToken)
        }
    }

    private fun removeChangeListener(token: DelegatedListenerToken) {
        val ref = when (token.type) {
            ListenerTokenType.REPLICATOR -> changeListeners.getOrNull(token.index)
            ListenerTokenType.DOCUMENT_REPLICATION -> documentChangeListeners.getOrNull(token.index)
            else -> error("${token.type} change listener can't be removed from Replicator instance")
        }
        if (ref != null) {
            CBLListener_Remove(token.actual)
            when (token.type) {
                ListenerTokenType.REPLICATOR -> removeListener(changeListeners, token.index)
                ListenerTokenType.DOCUMENT_REPLICATION -> removeListener(documentChangeListeners, token.index)
                else -> error("${token.type} change listener can't be removed from Replicator instance")
            }
        }
    }
}
