@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

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
