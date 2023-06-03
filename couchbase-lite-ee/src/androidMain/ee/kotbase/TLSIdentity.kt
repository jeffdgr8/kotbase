package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toDate
import kotlinx.datetime.Instant

public actual class TLSIdentity
internal constructor(actual: com.couchbase.lite.TLSIdentity) :
    DelegatedClass<com.couchbase.lite.TLSIdentity>(actual) {

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? {
            return com.couchbase.lite.TLSIdentity.getIdentity(alias)?.asTLSIdentity()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity {
            return com.couchbase.lite.TLSIdentity.createIdentity(
                isServer,
                attributes,
                expiration?.toDate(),
                alias
            ).asTLSIdentity()
        }
    }
}

internal actual val TLSIdentity.actual: com.couchbase.lite.TLSIdentity
    get() = actual

internal actual fun com.couchbase.lite.TLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
