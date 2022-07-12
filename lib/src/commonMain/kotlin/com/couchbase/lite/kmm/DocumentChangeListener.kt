package com.couchbase.lite.kmm

/**
 * The listener interface for receiving Document change events.
 *
 * Callback function from Database when the specified document is updated.
 *
 * @param change description of the change
 */
public typealias DocumentChangeListener = ChangeListener<DocumentChange>
