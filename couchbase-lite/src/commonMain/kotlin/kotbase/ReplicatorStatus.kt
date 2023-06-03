package kotbase

/**
 * Combined activity level and progress of a replicator.
 */
public expect class ReplicatorStatus {

    /**
     * The current activity level.
     */
    public val activityLevel: ReplicatorActivityLevel

    /**
     * The current progress of the replicator.
     */
    public val progress: ReplicatorProgress

    public val error: CouchbaseLiteException?
}
