package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ArrayExpressionIn
internal constructor(actual: com.couchbase.lite.ArrayExpressionIn) :
    DelegatedClass<com.couchbase.lite.ArrayExpressionIn>(actual) {

    public actual fun `in`(expression: Expression): ArrayExpressionSatisfies =
        ArrayExpressionSatisfies(actual.`in`(expression.actual))
}
