package com.couchbase.lite.kmm

/**
 * Combined activity level and progress of a replicator.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
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
