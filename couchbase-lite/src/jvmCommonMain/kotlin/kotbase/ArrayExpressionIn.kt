package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ArrayExpressionIn as CBLArrayExpressionIn

public actual class ArrayExpressionIn
internal constructor(actual: CBLArrayExpressionIn) : DelegatedClass<CBLArrayExpressionIn>(actual) {

    public actual fun `in`(expression: Expression): ArrayExpressionSatisfies =
        ArrayExpressionSatisfies(actual.`in`(expression.actual))
}
