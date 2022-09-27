package com.couchbase.lite.kmp

public actual open class Collation {

    protected var ignCase: Boolean = false

    public actual class ASCII : Collation() {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            this.ignCase = ignCase
            return this
        }
    }

    public actual class Unicode : Collation() {

        private var locale: String? = null
        private var ignAccents: Boolean = false

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
