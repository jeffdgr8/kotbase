@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * Progress of a replicator. If `total` is zero, the progress is indeterminate; otherwise,
 * dividing the two will produce a fraction that can be used to draw a progress bar.
 */
public expect class ReplicatorProgress {

    /**
     * The number of completed changes processed.
     */
    public val completed: Long

    /**
     * The total number of changes to be processed.
     */
    public val total: Long
}
