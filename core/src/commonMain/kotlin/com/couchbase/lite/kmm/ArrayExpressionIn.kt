package com.couchbase.lite.kmm

/**
 * The In class represents the IN clause object in a quantified operator (ANY/ANY AND EVERY/EVERY
 * &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;). The IN clause is used for specifying an array
 * object or an expression evaluated as an array object, each item of which will be evaluated
 * against the satisfies expression.
 */
public expect class ArrayExpressionIn {

    /**
     * Creates a Satisfies clause object with the given IN clause expression that could be an
     * array object or an expression evaluated as an array object.
     *
     * @param expression the expression evaluated as an array object.
     * @return A Satisfies object.
     */
    public fun `in`(expression: Expression): ArrayExpressionSatisfies
}
