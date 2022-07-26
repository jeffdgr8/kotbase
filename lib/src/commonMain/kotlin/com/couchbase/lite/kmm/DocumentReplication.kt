package com.couchbase.lite.kmm

/**
 * Document replicated update of a replicator.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
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
