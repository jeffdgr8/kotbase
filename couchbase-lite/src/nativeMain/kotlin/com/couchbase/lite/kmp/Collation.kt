package com.couchbase.lite.kmp

public actual open class Collation(private val isUnicode: Boolean) {

    protected var locale: String? = null
    protected var ignAccents: Boolean = false
    protected var ignCase: Boolean = false

    internal fun asJSON(): Dictionary {
        return MutableDictionary().apply {
            setBoolean("UNICODE", isUnicode)
            setString("LOCALE", locale)
            setBoolean("CASE", ignCase)
            setBoolean("DIAC", ignAccents)
        }
    }

    public actual class ASCII : Collation(false) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            this.ignCase = ignCase
            return this
        }
    }

    public actual class Unicode : Collation(true) {

        public actual fun setLocale(locale: String?): Unicode {
            this.locale = locale
            return this
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode {
            this.ignAccents = ignAccents
            return this
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode {
            this.ignCase = ignCase
            return this
        }
    }

    public actual companion object {

        public actual fun ascii(): ASCII = ASCII()

        public actual fun unicode(): Unicode = Unicode()
    }
}