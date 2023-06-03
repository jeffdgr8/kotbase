package kotbase

public actual class ArrayExpressionIn
internal constructor(
    private val type: ArrayExpression.QuantifiesType,
    private val variable: VariableExpression
) {

    public actual fun `in`(expression: Expression): ArrayExpressionSatisfies =
        ArrayExpressionSatisfies(type, variable, expression)
}
