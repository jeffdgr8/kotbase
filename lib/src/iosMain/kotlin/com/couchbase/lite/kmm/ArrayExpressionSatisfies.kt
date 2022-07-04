package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryExpression
import cocoapods.CouchbaseLite.CBLQueryVariableExpression

public actual class ArrayExpressionSatisfies
internal constructor(
    private val function: (CBLQueryVariableExpression, CBLQueryExpression, CBLQueryExpression) -> CBLQueryExpression,
    private val variable: CBLQueryVariableExpression,
    private val inExpression: CBLQueryExpression
) {

    public actual fun satisfies(expression: Expression): Expression =
        Expression(function(variable, inExpression, expression.actual))
}
