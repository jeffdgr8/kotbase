package kotbase

import cocoapods.CouchbaseLite.CBLFullTextIndexConfiguration
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.currentLocale

public actual class FullTextIndexConfiguration
private constructor(override var actual: CBLFullTextIndexConfiguration) : IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        CBLFullTextIndexConfiguration(
            expressions.toList(),
            false,
            NSLocale.currentLocale.objectForKey(NSLocaleLanguageCode) as String?
        )
    )

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        this.language = language
        return this
    }

    public actual var language: String?
        get() = actual.language
        set(value) {
            actual = CBLFullTextIndexConfiguration(
                actual.expressions,
                actual.ignoreAccents,
                value
            )
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    public actual var isIgnoringAccents: Boolean
        get() = actual.ignoreAccents
        set(value) {
            actual = CBLFullTextIndexConfiguration(
                actual.expressions,
                value,
                actual.language
            )
        }
}
