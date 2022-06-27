package com.couchbase.lite.kmm

// TODO: https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
public expect class FullTextIndexConfiguration(vararg expressions: String) : IndexConfiguration {

    /**
     * The language code which is an ISO-639 language such as "en", "fr", etc.
     * Setting the language code affects how word breaks and word stems are parsed.
     * Without setting the value, the current locale's language will be used. Setting
     * a null or "" value to disable the language features.
     */
    public fun setLanguage(language: String?): FullTextIndexConfiguration

    /**
     * Set the true value to ignore accents/diacritical marks. The default value is false.
     */
    public fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration
}
