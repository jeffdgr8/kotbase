@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * QueryChange contains the information about the query result changes reported
 * by a query object.
 */
public expect class QueryChange {

    /**
     * The source live query object.
     */
    public val query: Query

    /**
     * The new query result.
     */
    public val results: ResultSet?

    /**
     * The error occurred when running the query.
     */
    public val error: Throwable?
}
