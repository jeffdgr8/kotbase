package com.couchbase.lite.kmm

/**
 * Value Index Item
 */
public expect class ValueIndexItem {

    public companion object {

        /**
         * Creates a value index item with the given property.
         *
         * @param property the property name
         * @return The value index item
         */
        public fun property(property: String): ValueIndexItem

        /**
         * Creates a value index item with the given property.
         *
         * @param expression The expression to index. Typically a property expression.
         * @return The value index item
         */
        public fun expression(expression: Expression): ValueIndexItem
    }
}
