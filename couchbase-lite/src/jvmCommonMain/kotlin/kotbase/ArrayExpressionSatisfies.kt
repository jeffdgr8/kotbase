package kotbase

import com.couchbase.lite.ArrayExpressionSatisfies
import kotbase.base.DelegatedClass

public actual class ArrayExpressionSatisfies
internal constructor(actual: com.couchbase.lite.ArrayExpressionSatisfies) :
    DelegatedClass<ArrayExpressionSatisfies>(actual) {

    public actual fun satisfies(expression: Expression): Expression =
        Expression(actual.satisfies(expression.actual))
}
