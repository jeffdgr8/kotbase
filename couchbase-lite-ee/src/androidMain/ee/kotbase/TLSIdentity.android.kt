package kotbase

import com.couchbase.lite.deleteTLSIdentity
import kotbase.base.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotlinx.datetime.Instant
import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

public actual class TLSIdentity
internal constructor(actual: CBLTLSIdentity) : DelegatedClass<CBLTLSIdentity>(actual) {

    public actual val certs: List<ByteArray>
        get() = actual.certs.map { it.toByteArray() }

    public actual val expiration: Instant
        get() = actual.expiration.toKotlinInstant()

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? =
            CBLTLSIdentity.getIdentity(alias)?.asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity = CBLTLSIdentity.createIdentity(
            isServer,
            attributes,
            expiration?.toDate(),
            alias
        ).asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
            deleteTLSIdentity(alias)
        }
    }
}

internal actual val TLSIdentity.actual: CBLTLSIdentity
    get() = actual

internal actual fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
