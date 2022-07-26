package com.couchbase.lite.kmm

/**
 * The Satisfies class represents the SATISFIES clause object in a quantified operator
 * (ANY/ANY AND EVERY/EVERY &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;).
 * The SATISFIES clause is used for specifying an expression that will be used to evaluate
 * each item in the array.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class ArrayExpressionSatisfies {

    /**
     * Creates a complete quantified operator with the given satisfies expression.
     *
     * @param expression Parameter expression: The satisfies expression used for evaluating each item in the array.
     * @return The quantified expression.
     */
    public fun satisfies(expression: Expression): Expression
}
