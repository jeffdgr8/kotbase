package com.couchbase.lite.kmp

public actual object ArrayExpression {

    public actual fun any(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(com.couchbase.lite.ArrayExpression.any(variable.actual))

    public actual fun every(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(com.couchbase.lite.ArrayExpression.every(variable.actual))

    public actual fun anyAndEvery(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(com.couchbase.lite.ArrayExpression.anyAndEvery(variable.actual))

    public actual fun variable(name: String): VariableExpression =
        VariableExpression(com.couchbase.lite.ArrayExpression.variable(name))
}
