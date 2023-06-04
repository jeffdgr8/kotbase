package kotbase

import com.couchbase.lite.VariableExpression as CBLVariableExpression

public actual class VariableExpression
internal constructor(override val actual: CBLVariableExpression) : Expression(actual)
