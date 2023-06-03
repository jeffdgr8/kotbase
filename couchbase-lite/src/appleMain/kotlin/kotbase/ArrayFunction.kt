package kotbase

import cocoapods.CouchbaseLite.CBLQueryArrayFunction

public actual object ArrayFunction {

    public actual fun contains(expression: Expression, value: Expression): Expression =
        Expression(CBLQueryArrayFunction.contains(expression.actual, value.actual))

    public actual fun length(expression: Expression): Expression =
        Expression(CBLQueryArrayFunction.length(expression.actual))
}
