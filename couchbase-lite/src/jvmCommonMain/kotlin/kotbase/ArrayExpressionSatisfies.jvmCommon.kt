package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ArrayExpressionSatisfies as CBLArrayExpressionSatisfies

public actual class ArrayExpressionSatisfies
internal constructor(actual: CBLArrayExpressionSatisfies) : DelegatedClass<CBLArrayExpressionSatisfies>(actual) {

    public actual fun satisfies(expression: Expression): Expression =
        ExpressionImpl(actual.satisfies(expression.actual))
}
