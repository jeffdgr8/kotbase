package com.couchbase.lite.kmp

import kotlinx.cinterop.*
import libcblite.CBLFullTextIndexConfiguration
import libcblite.kCBLJSONLanguage

public actual class FullTextIndexConfiguration
internal constructor(expressions: List<String>) : IndexConfiguration(expressions) {

    public actual constructor(vararg expressions: String) : this(expressions.toList())

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        this.language = language
        return this
    }

    public actual var language: String? = null

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    public actual var isIgnoringAccents: Boolean = false

    internal fun getActual(memScope: MemScope): CValue<CBLFullTextIndexConfiguration> {
        val expCstr = expressions.joinToString(separator = ",", prefix = "[", postfix = "]").cstr
        val langCstr = language?.cstr
        return cValue {
            expressionLanguage = kCBLJSONLanguage
            expressions.buf = expCstr.getPointer(memScope)
            expressions.size = expCstr.size.convert()
            language.buf = langCstr?.getPointer(memScope)
            language.size = langCstr?.size?.convert() ?: 0U
            ignoreAccents = isIgnoringAccents
        }
    }
}
