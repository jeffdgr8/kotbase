package com.couchbase.lite.kmm

/**
 * Array expression
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect object ArrayExpression {

    /**
     * Creates an ANY Quantified operator (ANY &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;)
     * with the given variable name. The method returns an IN clause object that is used for
     * specifying an array object or an expression evaluated as an array object, each item of
     * which will be evaluated against the satisfies expression.
     * The ANY operator returns TRUE if at least one of the items in the array satisfies the given
     * satisfies expression.
     *
     * @param variable The variable expression.
     * @return An In object
     */
    public fun any(variable: VariableExpression): ArrayExpressionIn

    /**
     * Creates an EVERY Quantified operator (EVERY &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;)
     * with the given variable name. The method returns an IN clause object
     * that is used for specifying an array object or an expression evaluated as an array object,
     * each of which will be evaluated against the satisfies expression.
     * The EVERY operator returns TRUE if the array is empty OR every item in the array
     * satisfies the given satisfies expression.
     *
     * @param variable The variable expression.
     * @return An In object.
     */
    public fun every(variable: VariableExpression): ArrayExpressionIn

    /**
     * Creates an ANY AND EVERY Quantified operator (ANY AND EVERY &lt;variable name&gt; IN &lt;expr&gt;
     * SATISFIES &lt;expr&gt;) with the given variable name. The method returns an IN clause object
     * that is used for specifying an array object or an expression evaluated as an array object,
     * each of which will be evaluated against the satisfies expression.
     * The ANY AND EVERY operator returns TRUE if the array is NOT empty, and at least one of
     * the items in the array satisfies the given satisfies expression.
     *
     * @param variable The variable expression.
     * @return An In object.
     */
    public fun anyAndEvery(variable: VariableExpression): ArrayExpressionIn

    /**
     * Creates a variable expression. The variable are used to represent each item in an array in the
     * quantified operators (ANY/ANY AND EVERY/EVERY &lt;variable name&gt; IN &lt;expr&gt; SATISFIES &lt;expr&gt;)
     * to evaluate expressions over an array.
     *
     * @param name The variable name
     * @return A variable expression
     */
    public fun variable(name: String): VariableExpression
}
