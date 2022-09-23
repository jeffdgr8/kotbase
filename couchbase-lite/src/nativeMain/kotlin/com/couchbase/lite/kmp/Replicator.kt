package com.couchbase.lite.kmp

import cnames.structs.CBLReplicator
import com.couchbase.lite.kmp.internal.fleece.keys
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.udobny.kmp.toList
import kotlinx.cinterop.*
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class Replicator
internal constructor(
    internal val actual: CPointer<CBLReplicator>,
    public actual val config: ReplicatorConfiguration
) {

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLReplicator_Release(it)
    }

    public actual constructor(config: ReplicatorConfiguration) : this(
        wrapError { error ->
            CBLReplicator_Create(config.getActual(), error)!!
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

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(CBLReplicator_Status(actual))

    ///**
    // * The SSL/TLS certificate received when connecting to the server.
    // */
    //public val serverCertificate: SecCertificateRef?
    //    get() = actual.serverCertificate

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> = wrapError { error ->
        val dict = CBLReplicator_PendingDocumentIDs(actual, error)!!
        dict.keys().toSet().also {
            FLDict_Release(dict)
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean = wrapError { error ->
        CBLReplicator_IsDocumentPending(actual, docId.toFLString(), error)
    }

    private val changeListeners = mutableListOf<ReplicatorChangeListener?>()

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        val index = addChangeListener(changeListeners, listener)
        return DelegatedListenerToken(
            CBLReplicator_AddChangeListener(
                actual,
                staticCFunction { idx, _, status ->
                    changeListeners[idx.toLong().toInt()]!!(
                        ReplicatorChange(this, ReplicatorStatus(status!!))
                    )
                },
                index.toLong().toCPointer<CPointed>()
            )!!,
            ListenerTokenType.REPLICATOR,
            index
        )
    }

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    private val documentChangeListeners = mutableListOf<DocumentReplicationListener?>()

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        val index = addChangeListener(documentChangeListeners, listener)
        return DelegatedListenerToken(
            CBLReplicator_AddDocumentReplicationListener(
                actual,
                staticCFunction { idx, _, isPush, numDocuments, documents ->
                    documentChangeListeners[idx.toLong().toInt()]!!(
                        DocumentReplication(
                            this,
                            isPush,
                            documents!!.toList(numDocuments.toInt()) { ReplicatedDocument(it) }
                        )
                    )
                },
                index.toLong().toCPointer<CPointed>()
            )!!,
            ListenerTokenType.DOCUMENT_REPLICATION,
            index
        )
    }

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        CBLListener_Remove(token.actual)
        when (token.type) {
            ListenerTokenType.REPLICATOR -> removeChangeListener(changeListeners, token.index)
            ListenerTokenType.DOCUMENT_REPLICATION ->
                removeChangeListener(documentChangeListeners, token.index)
            else -> error("${token.type} change listener can't be removed from Replicator instance")
        }
    }
}
