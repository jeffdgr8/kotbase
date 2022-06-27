package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLFullTextIndexConfiguration

// TODO: https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
public actual class FullTextIndexConfiguration
internal constructor(override var actual: CBLFullTextIndexConfiguration) :
    IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        CBLFullTextIndexConfiguration(
            expressions.toList(),
            false,
            null
        )
    )

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        actual = CBLFullTextIndexConfiguration(
            actual.expressions,
            actual.ignoreAccents,
            language
        )
        return this
    }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        actual = CBLFullTextIndexConfiguration(
            actual.expressions,
            ignoreAccents,
            actual.language
        )
        return this
    }
}
