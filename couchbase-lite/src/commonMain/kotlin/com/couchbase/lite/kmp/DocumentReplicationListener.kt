package com.couchbase.lite.kmp

/**
 * The listener interface for receiving Document replicated events.
 *
 * The callback function from Replicator
 *
 * @param replication the Document replicated information
 */
public typealias DocumentReplicationListener = (replication: DocumentReplication) -> Unit