@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * A From represents a FROM clause for specifying the data source of the query.
 */
public expect class From : Query {

    /**
     * Creates and chains a Joins object for specifying the JOIN clause of the query.
     *
     * @param joins The Join objects.
     * @return The Joins object that represents the JOIN clause of the query.
     */
    public fun join(vararg joins: Join): Joins

    /**
     * Create and chain a WHERE component for specifying the WHERE clause of the query.
     *
     * @param expression the WHERE clause expression.
     * @return the WHERE component.
     */
    public fun where(expression: Expression): Where

    /**
     * Creates and chains a GroupBy object to group the query result.
     *
     * @param expressions The group by expression.
     * @return The GroupBy object that represents the GROUP BY clause of the query.
     */
    public fun groupBy(vararg expressions: Expression): GroupBy

    /**
     * Create and chain an ORDER BY component for specifying the ORDER BY clause of the query.
     *
     * @param orderings an array of the ORDER BY expressions.
     * @return the ORDER BY component.
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
