package com.couchbase.lite.kmp

import kotlinx.cinterop.*
import libcblite.CBLValueIndexConfiguration
import libcblite.kCBLJSONLanguage

public actual class ValueIndexConfiguration
internal constructor(expressions: List<String>) : IndexConfiguration(expressions) {

    public actual constructor(vararg expressions: String) : this(expressions.toList())

    internal fun getActual(memScope: MemScope): CValue<CBLValueIndexConfiguration> {
        val expCstr = expressions.joinToString(separator = ",", prefix = "[", postfix = "]").cstr
        return cValue {
            expressionLanguage = kCBLJSONLanguage
            expressions.buf = expCstr.getPointer(memScope)
            expressions.size = expCstr.size.convert()
        }
    }
}
