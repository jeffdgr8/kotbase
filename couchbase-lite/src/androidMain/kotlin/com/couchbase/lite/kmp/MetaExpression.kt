package com.couchbase.lite.kmp

public actual class MetaExpression
internal constructor(override val actual: com.couchbase.lite.MetaExpression) :
    Expression(actual) {

    public actual fun from(fromAlias: String): Expression =
        Expression(actual.from(fromAlias))
}
