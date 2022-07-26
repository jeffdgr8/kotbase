package com.couchbase.lite.kmm

/**
 * Provides details about a Database change.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
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
