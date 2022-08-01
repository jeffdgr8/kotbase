package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLFullTextIndex
import com.udobny.kmm.DelegatedClass

public actual class FullTextIndex
internal constructor(override val actual: CBLFullTextIndex) :
    DelegatedClass<CBLFullTextIndex>(actual), Index {

    public actual fun setLanguage(language: String?): FullTextIndex = chain {
        setLanguage(language)
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual.language = value
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex = chain {
        setIgnoreAccents(ignoreAccents)
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.ignoreAccents
        set(value) {
            actual.ignoreAccents = value
        }
}
