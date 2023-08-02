package kotbase

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
            return buildList {
                when (type) {
                    ArrayExpression.QuantifiesType.ANY -> add("ANY")
                    ArrayExpression.QuantifiesType.ANY_AND_EVERY -> add("ANY AND EVERY")
                    ArrayExpression.QuantifiesType.EVERY -> add("EVERY")
                }
                add(variable.name)
                add(inExpression.asJSON())
                add(satisfiesExpression.asJSON())
            }
        }
    }

    public actual fun satisfies(expression: Expression): Expression =
        QuantifiedExpression(type, variable, inExpression, expression)
}
