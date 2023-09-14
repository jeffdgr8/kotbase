package kotbase

import cocoapods.CouchbaseLite.CBLQueryVariableExpression

public actual class VariableExpression
internal constructor(actual: CBLQueryVariableExpression) : Expression(actual)

internal val VariableExpression.actual: CBLQueryVariableExpression
    get() = platformState.actual as CBLQueryVariableExpression
