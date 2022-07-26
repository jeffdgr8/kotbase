package com.couchbase.lite.kmm

/**
 * An OrderBy represents an ORDER BY clause of the query for specifying properties or expressions
 * that the result rows should be sorted by.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class OrderBy : Query {

    /**
     * Creates and chains a Limit object to limit the number query results.
     *
     * @param limit The limit expression.
     * @return The Limit object that represents the LIMIT clause of the query.
     */
    public fun limit(limit: Expression): Limit

    /**
     * Creates and chains a Limit object to skip the returned results for the given offset
     *
     * @param limit  The limit expression.
     * @param offset The offset expression.
     * @return The Limit object that represents the LIMIT clause of the query.
     */
    public fun limit(limit: Expression, offset: Expression?): Limit
}
