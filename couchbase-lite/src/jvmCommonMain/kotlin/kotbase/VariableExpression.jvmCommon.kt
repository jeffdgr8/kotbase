package kotbase

import com.couchbase.lite.VariableExpression as CBLVariableExpression

public actual class VariableExpression
internal constructor(actual: CBLVariableExpression) : Expression(actual)

internal val VariableExpression.actual: CBLVariableExpression
    get() = platformState!!.actual as CBLVariableExpression
