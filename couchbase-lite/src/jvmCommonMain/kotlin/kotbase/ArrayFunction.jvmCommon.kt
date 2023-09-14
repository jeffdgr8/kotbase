package kotbase

import com.couchbase.lite.ArrayFunction as CBLArrayFunction

public actual object ArrayFunction {

    public actual fun contains(expression: Expression, value: Expression): Expression =
        DelegatedExpression(CBLArrayFunction.contains(expression.actual, value.actual))

    public actual fun length(expression: Expression): Expression =
        DelegatedExpression(CBLArrayFunction.length(expression.actual))
}
