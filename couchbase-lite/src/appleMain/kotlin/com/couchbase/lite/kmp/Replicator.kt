package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicator
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toByteArray
import platform.Security.SecCertificateCopyData

public actual class Replicator
internal constructor(
    actual: CBLReplicator,
    private val _config: ReplicatorConfiguration
) : DelegatedClass<CBLReplicator>(actual) {

    public actual constructor(config: ReplicatorConfiguration) : this(
        CBLReplicator(config.actual),
        config
    )

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(config)

    public actual fun start() {
        actual.start()
    }

    public actual fun start(resetCheckpoint: Boolean) {
        actual.startWithReset(resetCheckpoint)
    }

    public actual fun stop() {
        actual.stop()
    }

    public actual val config: ReplicatorConfiguration
        get() = ReplicatorConfiguration(_config)

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(actual.status)

    public actual val serverCertificates: List<ByteArray>?
        get() = SecCertificateCopyData(actual.serverCertificate)?.toByteArray()?.let { listOf(it) }

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> {
        return wrapCBLError { error ->
            @Suppress("UNCHECKED_CAST")
            actual.pendingDocumentIDs(error) as Set<String>
        }
    }

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean {
        return wrapCBLError { error ->
            actual.isDocumentPending(docId, error)
        }
    }

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addChangeListener(listener.convert(this))
        )
    }

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken {
        return DelegatedListenerToken(
            actual.addDocumentReplicationListener(listener.convert(this))
        )
    }

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        token as DelegatedListenerToken
        actual.removeChangeListenerWithToken(token.actual)
    }
}
