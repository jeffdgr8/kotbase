package kotbase

import kotbase.base.DelegatedClass

public actual class ConnectionStatus
internal constructor(actual: com.couchbase.lite.ConnectionStatus) :
    DelegatedClass<com.couchbase.lite.ConnectionStatus>(actual) {

    public actual val connectionCount: Int
        get() = actual.connectionCount

    public actual val activeConnectionCount: Int
        get() = actual.activeConnectionCount
}

internal fun com.couchbase.lite.ConnectionStatus.asConnectionStatus(): ConnectionStatus =
    ConnectionStatus(this)
