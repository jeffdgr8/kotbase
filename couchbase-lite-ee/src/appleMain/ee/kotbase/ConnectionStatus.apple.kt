package kotbase

public actual class ConnectionStatus
internal constructor(
    public actual val connectionCount: Int,
    public actual val activeConnectionCount: Int
)