package com.couchbase.lite.kmp

import kotlinx.cinterop.*
import libcblite.CBLValueIndexConfiguration
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class ValueIndexConfiguration
internal constructor(expressions: List<String>) : IndexConfiguration(expressions) {

    public actual constructor(vararg expressions: String) : this(expressions.toList())

    internal fun getActual(): CValue<CBLValueIndexConfiguration> {
        val exp = expressions.joinToString(separator = ",")
        return cValue {
            expressionLanguage = kCBLN1QLLanguage
            expressions.buf = strdup(exp)
            expressions.size = strlen(exp)
        }
    }
}
