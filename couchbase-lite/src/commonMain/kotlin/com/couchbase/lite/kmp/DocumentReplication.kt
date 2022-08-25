@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * Document replicated update of a replicator.
 */
public expect class DocumentReplication {

    /**
     * The source replicator object.
     */
    public val replicator: Replicator

    /**
     * The current document replication direction flag.
     */
    public val isPush: Boolean

    /**
     * The list of affected documents.
     */
    public val documents: List<ReplicatedDocument>
}
