package kotbase

import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import libcblite.CBLFullTextIndexConfiguration
import libcblite.kCBLN1QLLanguage
import platform.posix.strdup
import platform.posix.strlen

public actual class FullTextIndexConfiguration
private constructor(expressions: List<String>) : IndexConfiguration(expressions) {

    public actual constructor(vararg expressions: String) : this(expressions.toList())

    public actual fun setLanguage(language: String?): FullTextIndexConfiguration {
        this.language = language
        return this
    }

    // TODO: this should default to device's default locale, tests check for "en"
    public actual var language: String? = "en"

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndexConfiguration {
        isIgnoringAccents = ignoreAccents
        return this
    }

    public actual var isIgnoringAccents: Boolean = false

    internal fun getActual(): CValue<CBLFullTextIndexConfiguration> {
        val exp = expressions.joinToString(separator = ",")
        val lang = language
        return cValue {
            expressionLanguage = kCBLN1QLLanguage
            expressions.buf = strdup(exp)
            expressions.size = strlen(exp)
            language.buf = lang?.let { strdup(it) }
            language.size = lang?.let { strlen(it) } ?: 0U
            ignoreAccents = isIgnoringAccents
        }
    }
}
