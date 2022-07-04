package com.couchbase.lite.kmm

/**
 * The listener interface for receiving Database change events.
 */
public fun interface DatabaseChangeListener : ChangeListener<DatabaseChange> {

    /**
     * Callback function from Database when database has change
     *
     * @param change the database change information
     */
    override fun changed(change: DatabaseChange)
}
