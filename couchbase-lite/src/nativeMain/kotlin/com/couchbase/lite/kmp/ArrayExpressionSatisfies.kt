package com.couchbase.lite.kmp

public actual class ArrayExpressionSatisfies
internal constructor(
    private val type: ArrayExpression.QuantifiesType,
    private val variable: VariableExpression,
    private val inExpression: Expression
) {

    private class QuantifiedExpression(
        val type: ArrayExpression.QuantifiesType,
        val variable: VariableExpression,
        val inExpression: Expression,
        val satisfiesExpression: Expression
    ) : Expression() {

        public override fun asJSON(): Any {
            return MutableArray().apply {
                when (type) {
                    ArrayExpression.QuantifiesType.ANY -> addString("ANY")
                    ArrayExpression.QuantifiesType.ANY_AND_EVERY -> addString("ANY AND EVERY")
                    ArrayExpression.QuantifiesType.EVERY -> addString("EVERY")
                }
                addString(variable.name)
                addValue(inExpression.asJSON())
                addValue(satisfiesExpression.asJSON())
            }
        }
    }

    public actual fun satisfies(expression: Expression): Expression =
        QuantifiedExpression(type, variable, inExpression, expression)
}
