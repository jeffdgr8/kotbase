package com.couchbase.lite.kmp

/**
 * Function provides array functions.
 */
public expect object ArrayFunction {

    /**
     * Creates an ARRAY_CONTAINS(expr, value) function that checks whether the given array
     * expression contains the given value or not.
     *
     * @param expression The expression that evaluate to an array.
     * @param value      The value to search for in the given array expression.
     * @return The ARRAY_CONTAINS(expr, value) function.
     */
    public fun contains(expression: Expression, value: Expression): Expression

    /**
     * Creates an ARRAY_LENGTH(expr) function that returns the length of the given array
     * expression.
     *
     * @param expression The expression that evaluates to an array.
     * @return The ARRAY_LENGTH(expr) function.
     */
    public fun length(expression: Expression): Expression
}
