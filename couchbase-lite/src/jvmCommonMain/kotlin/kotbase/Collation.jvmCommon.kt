package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Collation as CBLCollation

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
public actual open class Collation
private constructor(actual: CBLCollation) : DelegatedClass<CBLCollation>(actual) {

    public actual class ASCII
    internal constructor(override val actual: CBLCollation.ASCII) :
        Collation(actual) {

        public actual fun setIgnoreCase(ignCase: Boolean): ASCII {
            actual.setIgnoreCase(ignCase)
            return this
        }
    }

    public actual class Unicode
    internal constructor(override val actual: CBLCollation.Unicode) :
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
            ASCII(CBLCollation.ascii())

        public actual fun unicode(): Unicode =
            Unicode(CBLCollation.unicode())
    }
}
