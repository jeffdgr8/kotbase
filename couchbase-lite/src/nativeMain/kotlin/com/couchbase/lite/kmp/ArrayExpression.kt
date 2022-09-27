package com.couchbase.lite.kmp

public actual object ArrayExpression {

    internal enum class QuantifiesType {
        ANY, ANY_AND_EVERY, EVERY
    }

    public actual fun any(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(QuantifiesType.ANY, variable)

    public actual fun every(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(QuantifiesType.EVERY, variable)

    public actual fun anyAndEvery(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(QuantifiesType.ANY_AND_EVERY, variable)

    public actual fun variable(name: String): VariableExpression =
        VariableExpression(name)
}
