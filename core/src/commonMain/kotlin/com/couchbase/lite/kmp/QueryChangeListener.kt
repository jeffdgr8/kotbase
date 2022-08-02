package com.couchbase.lite.kmp

/**
 * The listener interface for receiving Live Query change events.
 *
 * The callback function from live query
 *
 * @param change the query change information
 */
public typealias QueryChangeListener = ChangeListener<QueryChange>
