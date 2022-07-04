package com.couchbase.lite.kmm

/**
 * The listener interface for receiving Live Query change events.
 */
public fun interface QueryChangeListener : ChangeListener<QueryChange> {

    /**
     * The callback function from live query
     *
     * @param change the query change information
     */
    override fun changed(change: QueryChange)
}
