package com.couchbase.lite.kmm

import java.util.Locale

public actual class FullTextIndexConfiguration
internal constructor(override val actual: com.couchbase.lite.FullTextIndexConfiguration) :
    IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        com.couchbase.lite.FullTextIndexConfiguration(*expressions)
    )

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        this.language = language
        return this
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var language: String? = Locale.getDefault().language
        set(value) {
            field = value
            actual.setLanguage(value)
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var isIgnoringAccents: Boolean = false
        set(value) {
            field = value
            actual.ignoreAccents(value)
        }
}
