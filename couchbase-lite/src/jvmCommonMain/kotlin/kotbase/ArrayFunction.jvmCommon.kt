package kotbase

import com.couchbase.lite.ArrayFunction as CBLArrayFunction

public actual object ArrayFunction {

    public actual fun contains(expression: Expression, value: Expression): Expression =
        ExpressionImpl(CBLArrayFunction.contains(expression.actual, value.actual))

    public actual fun length(expression: Expression): Expression =
        ExpressionImpl(CBLArrayFunction.length(expression.actual))
}
