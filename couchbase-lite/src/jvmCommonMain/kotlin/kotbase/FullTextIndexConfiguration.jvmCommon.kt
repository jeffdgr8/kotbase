package kotbase

import com.couchbase.lite.FullTextIndexConfiguration as CBLFullTextIndexConfiguration

public actual class FullTextIndexConfiguration
private constructor(actual: CBLFullTextIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(CBLFullTextIndexConfiguration(*expressions))

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        actual.language = language
        return this
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual.language = value
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        actual.ignoreAccents(ignoreAccents)
        return this
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.isIgnoringAccents
        set(value) {
            actual.ignoreAccents(value)
        }
}

internal val FullTextIndexConfiguration.actual: CBLFullTextIndexConfiguration
    get() = platformState!!.actual as CBLFullTextIndexConfiguration
