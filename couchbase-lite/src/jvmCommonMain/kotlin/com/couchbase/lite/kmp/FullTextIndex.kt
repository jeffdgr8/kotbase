package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import java.util.*

public actual class FullTextIndex
internal constructor(override val actual: com.couchbase.lite.FullTextIndex) :
    DelegatedClass<com.couchbase.lite.FullTextIndex>(actual), Index {

    public actual fun setLanguage(language: String?): FullTextIndex = chain {
        setLanguage(language)
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var language: String? = Locale.getDefault().language
        set(value) {
            field = value
            actual.setLanguage(value)
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex = chain {
        ignoreAccents(ignoreAccents)
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var isIgnoringAccents: Boolean = false
        set(value) {
            field = value
            actual.ignoreAccents(value)
        }
}
