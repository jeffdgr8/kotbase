package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.JsonUtils
import kotlinx.cinterop.*
import libcblite.CBLFullTextIndexConfiguration
import libcblite.kCBLN1QLLanguage

public actual class FullTextIndex
internal constructor(private val items: List<FullTextIndexItem>) : Index() {

    public actual fun setLanguage(language: String?): FullTextIndex {
        this.language = language
        return this
    }

    public actual var language: String? = null

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex {
        isIgnoringAccents = ignoreAccents
        return this
    }

    public actual var isIgnoringAccents: Boolean = false

    private fun getJson(): String {
        val data = buildList {
            items.forEach {
                add(it.expression.asJSON())
            }
        }
        return JsonUtils.toJson(data)
    }

    internal fun getActual(memScope: MemScope): CValue<CBLFullTextIndexConfiguration> {
        val expCstr = getJson().cstr
        val langCstr = language?.cstr
        return cValue {
            expressionLanguage = kCBLN1QLLanguage
            expressions.buf = expCstr.getPointer(memScope)
            expressions.size = expCstr.size.convert()
            language.buf = langCstr?.getPointer(memScope)
            language.size = langCstr?.size?.convert() ?: 0U
            ignoreAccents = isIgnoringAccents
        }
    }
}
