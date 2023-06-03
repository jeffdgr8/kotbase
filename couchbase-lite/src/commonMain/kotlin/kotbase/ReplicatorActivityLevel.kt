package kotbase

/**
 * Activity level of a replicator.
 */
public expect enum class ReplicatorActivityLevel {

    /**
     * The replication is finished or hit a fatal error.
     */
    STOPPED,

    /**
     * The replicator is offline because the remote host is unreachable.
     */
    OFFLINE,

    /**
     * The replicator is connecting to the remote host.
     */
    CONNECTING,

    /**
     * The replication is inactive; either waiting for changes or offline
     * as the remote host is unreachable.
     */
    IDLE,

    /**
     * The replication is actively transferring data.
     */
    BUSY
}
