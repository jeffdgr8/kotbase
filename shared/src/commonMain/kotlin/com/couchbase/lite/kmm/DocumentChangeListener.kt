package com.couchbase.lite.kmm

/**
 * The listener interface for receiving Document change events.
 */
public fun interface DocumentChangeListener : ChangeListener<DocumentChange> {

    /**
     * Callback function from Database when the specified document is updated.
     *
     * @param change description of the change
     */
    override fun changed(change: DocumentChange)
}
