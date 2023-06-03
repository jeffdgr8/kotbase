package kotbase

/**
 * A GroupBy represents the GROUP BY clause to group the query result.
 * The GROUP BY clause is normally used with aggregate functions (AVG, COUNT, MAX, MIN, SUM)
 * to aggregate the group of the values.
 */
public expect class GroupBy : Query {

    /**
     * Creates and chain a Having object for filtering the aggregated values
     * from the the GROUP BY clause.
     *
     * @param expression The expression
     * @return The Having object that represents the HAVING clause of the query.
     */
    public fun having(expression: Expression): Having

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
