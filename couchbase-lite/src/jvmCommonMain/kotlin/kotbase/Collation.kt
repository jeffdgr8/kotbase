package kotbase

import kotbase.base.DelegatedClass

public actual open class Collation
private constructor(actual: com.couchbase.lite.Collation) :
    DelegatedClass<com.couchbase.lite.Collation>(actual) {

    public actual class ASCII
    internal constructor(override val actual: com.couchbase.lite.Collation.ASCII) :
        Collation(actual) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            actual.setIgnoreCase(ignCase)
            return this
        }
    }

    public actual class Unicode
    internal constructor(override val actual: com.couchbase.lite.Collation.Unicode) :
        Collation(actual) {

        public actual fun setLocale(locale: String?): Unicode {
            actual.setLocale(locale)
            return this
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode {
            actual.setIgnoreAccents(ignAccents)
            return this
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode {
            actual.setIgnoreCase(ignCase)
            return this
        }
    }

    public actual companion object {

        public actual fun ascii(): ASCII =
            ASCII(com.couchbase.lite.Collation.ascii())

        public actual fun unicode(): Unicode =
            Unicode(com.couchbase.lite.Collation.unicode())
    }
}
