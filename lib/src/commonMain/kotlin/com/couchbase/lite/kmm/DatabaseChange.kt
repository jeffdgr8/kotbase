package com.couchbase.lite.kmm

/**
 * Provides details about a Database change.
 */
public expect class DatabaseChange {

    /**
     * The database instance
     */
    public val database: Database

    /**
     * The list of the changed document IDs
     */
    public val documentIDs: List<String>
}
