package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLQueryArrayExpression

public actual object ArrayExpression {

    public actual fun any(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLQueryArrayExpression.Companion::any, variable.actual)

    public actual fun every(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLQueryArrayExpression.Companion::every, variable.actual)

    public actual fun anyAndEvery(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLQueryArrayExpression.Companion::anyAndEvery, variable.actual)

    public actual fun variable(name: String): VariableExpression =
        VariableExpression(CBLQueryArrayExpression.variableWithName(name))
}
