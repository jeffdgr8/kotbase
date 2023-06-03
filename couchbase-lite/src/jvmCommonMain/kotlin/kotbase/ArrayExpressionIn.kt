package kotbase

import com.couchbase.lite.ArrayExpressionIn
import kotbase.base.DelegatedClass

public actual class ArrayExpressionIn
internal constructor(actual: com.couchbase.lite.ArrayExpressionIn) :
    DelegatedClass<ArrayExpressionIn>(actual) {

    public actual fun `in`(expression: Expression): ArrayExpressionSatisfies =
        ArrayExpressionSatisfies(actual.`in`(expression.actual))
}
