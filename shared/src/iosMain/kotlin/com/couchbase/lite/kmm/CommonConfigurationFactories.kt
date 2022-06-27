@file:Suppress("NAME_SHADOWING")

package com.couchbase.lite.kmm

public actual fun FullTextIndexConfiguration?.create(
    language: String?,
    ignoreAccents: Boolean?,
    vararg expressions: String
): FullTextIndexConfiguration {
    val expressions = if (expressions.isNotEmpty()) {
        expressions
    } else {
        @Suppress("UNCHECKED_CAST")
        (this?.actual?.expressions as List<String>?)?.toTypedArray()
    } ?: error("Must specify an expression")
    return FullTextIndexConfiguration(
        *expressions
    ).apply {
        language?.let { setLanguage(it) }
        ignoreAccents?.let { ignoreAccents(it) }
    }
}

public actual fun ValueIndexConfiguration?.create(
    vararg expressions: String
): ValueIndexConfiguration {
    val expressions = if (expressions.isNotEmpty()) {
        expressions
    } else {
        @Suppress("UNCHECKED_CAST")
        (this?.actual?.expressions as List<String>?)?.toTypedArray()
    } ?: error("Must specify an expression")
    return ValueIndexConfiguration(
        *expressions
    )
}
