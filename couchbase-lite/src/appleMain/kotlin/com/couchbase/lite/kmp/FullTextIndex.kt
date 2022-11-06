package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLFullTextIndex

public actual class FullTextIndex
internal constructor(override val actual: CBLFullTextIndex) : Index(actual) {

    public actual fun setLanguage(language: String?): FullTextIndex {
        actual.setLanguage(language)
        return this
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual.language = value
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex {
        actual.setIgnoreAccents(ignoreAccents)
        return this
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.ignoreAccents
        set(value) {
            actual.ignoreAccents = value
        }
}
