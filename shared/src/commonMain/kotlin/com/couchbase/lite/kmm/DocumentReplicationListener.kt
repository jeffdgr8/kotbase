package com.couchbase.lite.kmm

/**
 * The listener interface for receiving Document replicated events.
 */
public fun interface DocumentReplicationListener {

    /**
     * The callback function from Replicator
     *
     * @param replication the Document replicated information
     */
    public fun replication(replication: DocumentReplication)
}
