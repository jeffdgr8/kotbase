package kotbase

public actual object ArrayFunction {

    public actual fun contains(expression: Expression, value: Expression): Expression =
        Expression.FunctionExpression("ARRAY_CONTAINS()", listOf(expression, value))

    public actual fun length(expression: Expression): Expression =
        Expression.FunctionExpression("ARRAY_LENGTH()", listOf(expression))
}
