@file:Suppress("NO_ACTUAL_FOR_EXPECT") // https://youtrack.jetbrains.com/issue/KT-42466

package com.couchbase.lite.kmp

/**
 * Index for Full-Text search
 */
public expect class FullTextIndex : Index {

    /**
     * The language code which is an ISO-639 language such as "en", "fr", etc.
     * Setting the language code affects how word breaks and word stems are parsed.
     * Without setting the value, the current locale's language will be used. Setting
     * a nil or "" value to disable the language features.
     */
    public fun setLanguage(language: String?): FullTextIndex

    public var language: String?

    /**
     * Set the true value to ignore accents/diacritical marks. The default value is false.
     */
    public fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex

    public var isIgnoringAccents: Boolean
}
