package com.couchbase.lite.kmm

/**
 * The listener interface for receiving Replicator change events.
 */
public fun interface ReplicatorChangeListener {

    /**
     * The callback function from Replicator
     *
     * @param change the Replicator change information
     */
    public fun changed(change: ReplicatorChange)
}
