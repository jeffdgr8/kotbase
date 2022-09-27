package com.couchbase.lite.kmp

/**
 * An Ordering represents a single ordering component in the query ORDER BY clause.
 */
public expect abstract class Ordering {

    /**
     * SortOrder represents a single ORDER BY entity. You can specify either ascending or
     * descending order. The default order is ascending.
     */
    public class SortOrder : Ordering {

        /**
         * Set the order as ascending order.
         *
         * @return the OrderBy object.
         */
        public fun ascending(): Ordering

        /**
         * Set the order as descending order.
         *
         * @return the OrderBy object.
         */
        public fun descending(): Ordering
    }

    public companion object {

        /**
         * Create a SortOrder, inherited from the OrderBy class, object by the given
         * property name.
         *
         * @param property the property name
         * @return the SortOrder object.
         */
        public fun property(property: String): SortOrder

        /**
         * Create a SortOrder, inherited from the OrderBy class, object by the given expression.
         *
         * @param expression the expression object.
         * @return the SortOrder object.
         */
        public fun expression(expression: Expression): SortOrder
    }
}
