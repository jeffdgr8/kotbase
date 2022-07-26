package com.couchbase.lite.kmm

/**
 * ReplicatorChange contains the replicator status information.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
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
