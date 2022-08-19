package com.couchbase.lite.kmp

import com.udobny.kmp.chain

public actual class FullTextIndexConfiguration
internal constructor(override val actual: com.couchbase.lite.FullTextIndexConfiguration) :
    IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        com.couchbase.lite.FullTextIndexConfiguration(*expressions)
    )

    private inline fun chain(action: com.couchbase.lite.FullTextIndexConfiguration.() -> Unit) =
        chain(actual, action)

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration = chain {
        setLanguage(language)
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual.language = value
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration = chain {
        ignoreAccents(ignoreAccents)
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.isIgnoringAccents
        set(value) {
            actual.ignoreAccents(value)
        }
}
