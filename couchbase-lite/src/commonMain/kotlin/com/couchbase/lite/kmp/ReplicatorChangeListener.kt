package com.couchbase.lite.kmp

/**
 * The listener interface for receiving Replicator change events.
 *
 * The callback function from Replicator
 *
 * @param change the Replicator change information
 */
public typealias ReplicatorChangeListener = (change: ReplicatorChange) -> Unit