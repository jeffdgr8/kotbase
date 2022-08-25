@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

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
