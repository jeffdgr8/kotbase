package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ConnectionStatus as CBLConnectionStatus

public actual class ConnectionStatus
internal constructor(actual: CBLConnectionStatus) : DelegatedClass<CBLConnectionStatus>(actual) {

    public actual val connectionCount: Int
        get() = actual.connectionCount

    public actual val activeConnectionCount: Int
        get() = actual.activeConnectionCount
}

internal fun CBLConnectionStatus.asConnectionStatus(): ConnectionStatus =
    ConnectionStatus(this)
