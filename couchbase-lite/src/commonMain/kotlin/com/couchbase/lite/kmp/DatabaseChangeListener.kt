package com.couchbase.lite.kmp

/**
 * The listener interface for receiving Database change events.
 *
 * Callback function from Database when database has change
 *
 * @param change the database change information
 */
public typealias DatabaseChangeListener = ChangeListener<DatabaseChange>
