package com.couchbase.lite.kmm

import com.udobny.kmm.chain

// TODO: https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
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

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration = chain {
        ignoreAccents(ignoreAccents)
    }
}
