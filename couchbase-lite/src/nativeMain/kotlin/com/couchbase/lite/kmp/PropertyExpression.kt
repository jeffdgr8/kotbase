package com.couchbase.lite.kmp

public actual class PropertyExpression
internal constructor(
    private val keyPath: String,
    private val fromAlias: String? = null
) : Expression() {

    public actual fun from(fromAlias: String): Expression =
        PropertyExpression(keyPath, fromAlias)

    override fun asJSON(): Any =
        listOf(".${if (fromAlias == null) "" else "$fromAlias."}$keyPath")
}
