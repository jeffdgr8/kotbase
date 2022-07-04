package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryVariableExpression

public actual class VariableExpression
internal constructor(override val actual: CBLQueryVariableExpression) : Expression(actual)
