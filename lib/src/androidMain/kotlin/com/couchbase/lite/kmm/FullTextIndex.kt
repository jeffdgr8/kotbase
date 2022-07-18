package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class FullTextIndex
internal constructor(override val actual: com.couchbase.lite.FullTextIndex) :
    DelegatedClass<com.couchbase.lite.FullTextIndex>(actual), Index {

    public actual fun setLanguage(language: String?): FullTextIndex = chain {
        // TODO: should be @Nullable in Java API
        //  https://forums.couchbase.com/t/should-fulltextindex-setlanguage-be-nullable/33793
        if (language == null) {
            setLanguage("")
        } else {
            setLanguage(language)
        }
    }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex = chain {
        ignoreAccents(ignoreAccents)
    }
}
