package com.couchbase.lite.kmm

/**
 * A Joins component represents a collection of the joins clauses of the query statement.
 */
public expect class Joins : Query {

    /**
     * Creates and chains a Where object for specifying the WHERE clause of the query.
     *
     * @param expression The where expression.
     * @return The Where object that represents the WHERE clause of the query.
     */
    public fun where(expression: Expression): Where

    /**
     * Creates and chains an OrderBy object for specifying the orderings of the query result.
     *
     * @param orderings The Ordering objects.
     * @return The OrderBy object that represents the ORDER BY clause of the query.
     */
    public fun orderBy(vararg orderings: Ordering): OrderBy

    /**
     * Creates and chains a Limit object to limit the number query results.
     *
     * @param limit The limit expression.
     * @return The Limit object that represents the LIMIT clause of the query.
     */
    public fun limit(limit: Expression): Limit

    /**
     * Creates and chains a Limit object to skip the returned results for the given offset
     * position and to limit the number of results to not more than the given limit value.
     *
     * @param limit  The limit expression.
     * @param offset The offset expression.
     * @return The Limit object that represents the LIMIT clause of the query.
     */
    public fun limit(limit: Expression, offset: Expression?): Limit
}
