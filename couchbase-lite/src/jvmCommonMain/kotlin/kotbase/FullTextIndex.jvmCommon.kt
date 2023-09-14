package kotbase

import java.util.*
import com.couchbase.lite.FullTextIndex as CBLFullTextIndex

public actual class FullTextIndex
internal constructor(actual: CBLFullTextIndex) : Index(actual) {

    public actual fun setLanguage(language: String?): FullTextIndex {
        actual.setLanguage(language)
        return this
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var language: String? = Locale.getDefault().language
        set(value) {
            field = value
            actual.setLanguage(value)
        }

    public actual fun ignoreAccents(ignoreAccents: Boolean): FullTextIndex {
        actual.ignoreAccents(ignoreAccents)
        return this
    }

    // TODO: use actual getter instead of field in 3.1
    public actual var isIgnoringAccents: Boolean = false
        set(value) {
            field = value
            actual.ignoreAccents(value)
        }
}

internal val FullTextIndex.actual: CBLFullTextIndex
    get() = platformState!!.actual as CBLFullTextIndex
