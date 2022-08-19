package com.couchbase.lite.kmp

public actual object ArrayFunction {

    public actual fun contains(expression: Expression, value: Expression): Expression =
        Expression(com.couchbase.lite.ArrayFunction.contains(expression.actual, value.actual))

    public actual fun length(expression: Expression): Expression =
        Expression(com.couchbase.lite.ArrayFunction.length(expression.actual))
}
