package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ArrayExpressionSatisfies
internal constructor(actual: com.couchbase.lite.ArrayExpressionSatisfies) :
    DelegatedClass<com.couchbase.lite.ArrayExpressionSatisfies>(actual) {

    public actual fun satisfies(expression: Expression): Expression =
        Expression(actual.satisfies(expression.actual))
}
