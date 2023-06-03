package kotbase

/**
 * ReplicatorChange contains the replicator status information.
 */
public expect class ReplicatorChange {

    /**
     * Return the source replicator object.
     */
    public val replicator: Replicator

    /**
     * Return the replicator status.
     */
    public val status: ReplicatorStatus
}
