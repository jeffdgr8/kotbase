package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import java.security.cert.Certificate

public actual class Replicator
internal constructor(actual: com.couchbase.lite.Replicator) :
    DelegatedClass<com.couchbase.lite.Replicator>(actual) {

    public actual constructor(config: ReplicatorConfiguration) : this(
        com.couchbase.lite.Replicator(config.actual)
    )

    public actual fun start() {
        actual.start()
    }

    public actual fun start(resetCheckpoint: Boolean) {
        actual.start(resetCheckpoint)
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
     * The server certificates received from the server during the TLS handshake.
     */
    public val serverCertificates: List<Certificate>?
        get() = actual.serverCertificates

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> =
        actual.pendingDocumentIds

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean =
        actual.isDocumentPending(docId)

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert())

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken =
        actual.addDocumentReplicationListener(listener.convert())

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListener(token)
    }
}