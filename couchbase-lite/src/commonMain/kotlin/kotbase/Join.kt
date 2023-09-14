package kotbase

internal expect class JoinPlatformState

/**
 * A Join component representing a single JOIN clause in the query statement.
 */
public expect open class Join {

    internal val platformState: JoinPlatformState

    /**
     * Component used for specifying join on conditions.
     */
    public class On : Join {

        /**
         * Specify join conditions from the given expression.
         *
         * @param expression The Expression object specifying the join conditions.
         * @return The Join object that represents a single JOIN clause of the query.
         */
        public fun on(expression: Expression): Join
    }

    public companion object {

        /**
         * Create a JOIN (same as INNER JOIN) component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun join(datasource: DataSource): On

        /**
         * Create an INNER JOIN component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun innerJoin(datasource: DataSource): On

        /**
         * Create a LEFT JOIN (same as LEFT OUTER JOIN) component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun leftJoin(datasource: DataSource): On

        /**
         * Create a LEFT OUTER JOIN component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The On object used for specifying join conditions.
         */
        public fun leftOuterJoin(datasource: DataSource): On

        /**
         * Create an CROSS JOIN component with the given data source.
         * Use the returned On component to specify join conditions.
         *
         * @param datasource The DataSource object of the JOIN clause.
         * @return The Join object used for specifying join conditions.
         */
        public fun crossJoin(datasource: DataSource): Join
    }
}
