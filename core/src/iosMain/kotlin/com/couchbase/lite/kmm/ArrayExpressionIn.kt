package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.CBLQueryVariableExpression

public actual class ArrayExpressionIn
internal constructor(
    private val function: (CBLQueryVariableExpression, CBLQueryExpression, CBLQueryExpression) -> CBLQueryExpression,
    private val variable: CBLQueryVariableExpression
) {

    public actual fun `in`(expression: Expression): ArrayExpressionSatisfies =
        ArrayExpressionSatisfies(function, variable, expression.actual)
}
