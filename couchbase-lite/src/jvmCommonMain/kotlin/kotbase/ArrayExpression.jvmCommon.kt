package kotbase

import com.couchbase.lite.ArrayExpression as CBLArrayExpression

public actual object ArrayExpression {

    public actual fun any(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLArrayExpression.any(variable.actual))

    public actual fun every(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLArrayExpression.every(variable.actual))

    public actual fun anyAndEvery(variable: VariableExpression): ArrayExpressionIn =
        ArrayExpressionIn(CBLArrayExpression.anyAndEvery(variable.actual))

    public actual fun variable(name: String): VariableExpression =
        VariableExpression(CBLArrayExpression.variable(name))
}
