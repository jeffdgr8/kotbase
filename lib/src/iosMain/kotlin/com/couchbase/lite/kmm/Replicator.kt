package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLReplicator
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.internal.testQueue
import com.couchbase.lite.kmm.internal.useTestQueue
import com.udobny.kmm.DelegatedClass
import platform.Security.SecCertificateRef

public actual class Replicator
internal constructor(actual: CBLReplicator) :
    DelegatedClass<CBLReplicator>(actual) {

    public actual constructor(config: ReplicatorConfiguration) : this(CBLReplicator(config.actual))

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
    public actual fun getPendingDocumentIds(): Set<String> = throwError { error ->
        @Suppress("UNCHECKED_CAST")
        pendingDocumentIDs(error) as Set<String>
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean = throwError { error ->
        isDocumentPending(docId, error)
    }

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        return DelegatedListenerToken(
            if (useTestQueue) {
                actual.addChangeListenerWithQueue(testQueue, listener.convert())
            } else {
                actual.addChangeListener(listener.convert())
            }
        )
    }

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        return DelegatedListenerToken(
            if (useTestQueue) {
                actual.addDocumentReplicationListenerWithQueue(testQueue, listener.convert())
            } else {
                actual.addDocumentReplicationListener(listener.convert())
            }
        )
    }

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListenerWithToken(token.actual)
    }
}
