package kotbase

/**
 * Connection Status
 */
public expect class ConnectionStatus {

    /**
     * The count of clients currently connected to this listener.
     */
    public val connectionCount: Int

    /**
     * The count of clients that are currently actively transferring data.
     * Note: this number is highly volatile.  The actual number of active connections
     * may have changed by the time the call returns.
     */
    public val activeConnectionCount: Int
}
