package kotbase

import cocoapods.CouchbaseLite.CBLQueryArrayFunction

public actual object ArrayFunction {

    public actual fun contains(expression: Expression, value: Expression): Expression =
        DelegatedExpression(CBLQueryArrayFunction.contains(expression.actual, value.actual))

    public actual fun length(expression: Expression): Expression =
        DelegatedExpression(CBLQueryArrayFunction.length(expression.actual))
}
