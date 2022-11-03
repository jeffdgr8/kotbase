package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicator
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.DelegatedClass
import platform.Security.SecCertificateRef

public actual class Replicator
internal constructor(actual: CBLReplicator) :
    DelegatedClass<CBLReplicator>(actual) {

    public actual constructor(config: ReplicatorConfiguration) : this(CBLReplicator(config.actual))

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(
        CBLReplicator(config.actual)
    )

    public actual fun start() {
        actual.start()
    }

    public actual fun start(resetCheckpoint: Boolean) {
        actual.startWithReset(resetCheckpoint)
    }

    public actual fun stop() {
        actual.stop()
    }

    public actual val config: ReplicatorConfiguration by lazy {
        ReplicatorConfiguration(actual.config)
    }

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(actual.status)

    /**
     * The SSL/TLS certificate received when connecting to the server.
     */
    public val serverCertificate: SecCertificateRef?
        get() = actual.serverCertificate

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> = wrapCBLError { error ->
        @Suppress("UNCHECKED_CAST")
        pendingDocumentIDs(error) as Set<String>
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean = wrapCBLError { error ->
        isDocumentPending(docId, error)
    }

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addChangeListener(listener.convert())
        )
    }

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addDocumentReplicationListener(listener.convert())
        )
    }

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        actual.removeChangeListenerWithToken(token.actual)
    }
}
