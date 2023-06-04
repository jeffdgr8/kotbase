package kotbase

import com.couchbase.lite.testReplicator
import kotbase.base.DelegatedClass
import com.couchbase.lite.Replicator as CBLReplicator

public actual class Replicator
internal constructor(
    actual: CBLReplicator,
    private val _config: ReplicatorConfiguration
) : DelegatedClass<CBLReplicator>(actual) {

    public actual constructor(config: ReplicatorConfiguration) : this(CBLReplicator(config.actual), config)

    internal actual constructor(config: ReplicatorConfiguration, test: Boolean) : this(
        if (test) {
            testReplicator(config.actual)
        } else {
            CBLReplicator(config.actual)
        },
        config
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

    public actual val config: ReplicatorConfiguration
        get() = ReplicatorConfiguration(_config)

    public actual val status: ReplicatorStatus
        get() = ReplicatorStatus(actual.status)

    public actual val serverCertificates: List<ByteArray>?
        get() = actual.serverCertificates?.map { it.encoded }

    @Throws(CouchbaseLiteException::class)
    public actual fun getPendingDocumentIds(): Set<String> =
        actual.pendingDocumentIds

    @Throws(CouchbaseLiteException::class)
    public actual fun isDocumentPending(docId: String): Boolean =
        actual.isDocumentPending(docId)

    public actual fun addChangeListener(listener: ReplicatorChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert(this))

    // TODO:
    //public actual fun addChangeListener(executor: Executor?, listener: ReplicatorChangeListener): ListenerToken

    public actual fun addDocumentReplicationListener(listener: DocumentReplicationListener): ListenerToken =
        actual.addDocumentReplicationListener(listener.convert(this))

    // TODO:
    //public actual fun addDocumentReplicationListener(executor: Executor?, listener: DocumentReplicationListener): ListenerToken

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListener(token)
    }
}
