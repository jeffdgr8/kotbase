@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

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
