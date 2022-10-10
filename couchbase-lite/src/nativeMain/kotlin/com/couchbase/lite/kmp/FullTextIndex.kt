package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.JsonUtils
import kotlinx.cinterop.*
import libcblite.CBLFullTextIndexConfiguration
import libcblite.kCBLJSONLanguage
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class FullTextIndex
internal constructor(private val items: List<FullTextIndexItem>) : Index() {

    public actual fun setLanguage(language: String?): FullTextIndex {
        this.language = language
        return this
    }

    // TODO: this should default to device's default locale, tests check for "en"
    public actual var language: String? = "en"

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

    internal fun getActual(): CValue<CBLFullTextIndexConfiguration> {
        val json = getJson()
        val lang = language
        return cValue {
            expressionLanguage = kCBLJSONLanguage
            expressions.buf = strdup(json)
            expressions.size = strlen(json)
            language.buf = lang?.let { strdup(it) }
            language.size = lang?.let { strlen(it) } ?: 0U
            ignoreAccents = isIgnoringAccents
        }
    }
}
