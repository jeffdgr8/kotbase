package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryExpression

public actual class PropertyExpression
internal constructor(private val property: String) :
    Expression(CBLQueryExpression.property(property)) {

    public actual fun from(fromAlias: String): Expression =
        Expression(CBLQueryExpression.property(property, fromAlias))
}
