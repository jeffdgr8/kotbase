package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.chain

public actual open class Collation
private constructor(actual: com.couchbase.lite.Collation) :
    DelegatedClass<com.couchbase.lite.Collation>(actual) {

    public actual class ASCII
    internal constructor(override val actual: com.couchbase.lite.Collation.ASCII) :
        Collation(actual) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII = chain {
            actual.setIgnoreCase(ignCase)
        }
    }

    public actual class Unicode
    internal constructor(override val actual: com.couchbase.lite.Collation.Unicode) :
        Collation(actual) {

        private inline fun chain(action: com.couchbase.lite.Collation.Unicode.() -> Unit) =
            chain(actual, action)

        public actual fun setLocale(locale: String?): Unicode = chain {
            setLocale(locale)
        }

        public actual fun setIgnoreAccents(ignAccents: Boolean): Unicode = chain {
            setIgnoreAccents(ignAccents)
        }

        public actual fun setIgnoreCase(ignCase: Boolean): Unicode = chain {
            setIgnoreCase(ignCase)
        }
    }

    public actual companion object {

        public actual fun ascii(): ASCII =
            ASCII(com.couchbase.lite.Collation.ascii())

        public actual fun unicode(): Unicode =
            Unicode(com.couchbase.lite.Collation.unicode())
    }
}
