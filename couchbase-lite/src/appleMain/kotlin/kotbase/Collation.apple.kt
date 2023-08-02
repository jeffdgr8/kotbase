package kotbase

import cocoapods.CouchbaseLite.CBLQueryCollation
import kotbase.base.DelegatedClass

public actual open class Collation
private constructor(actual: CBLQueryCollation) :
    DelegatedClass<CBLQueryCollation>(actual) {

    public actual class ASCII
    internal constructor(
        override var actual: CBLQueryCollation =
            CBLQueryCollation.asciiWithIgnoreCase(false)
    ) : Collation(actual) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            actual = CBLQueryCollation.asciiWithIgnoreCase(ignCase)
            return this
        }
    }

    public actual class Unicode
    internal constructor(
        override var actual: CBLQueryCollation =
            CBLQueryCollation.unicodeWithLocale(null, ignoreCase = false, ignoreAccents = false)
    ) : Collation(actual) {

        private var locale: String? = null
        private var ignCase: Boolean = false
        private var ignAccents: Boolean = false

        public actual fun setLocale(locale: String?): Unicode {
            this.locale = locale
            actual = CBLQueryCollation.unicodeWithLocale(locale, ignCase, ignAccents)
            return this
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode {
            this.ignAccents = ignAccents
            actual = CBLQueryCollation.unicodeWithLocale(locale, ignCase, ignAccents)
            return this
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode {
            this.ignCase = ignCase
            actual = CBLQueryCollation.unicodeWithLocale(locale, ignCase, ignAccents)
            return this
        }
    }

    public actual companion object {

        public actual fun ascii(): ASCII = ASCII()

        public actual fun unicode(): Unicode = Unicode()
    }
}
