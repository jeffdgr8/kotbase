package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toDate
import kotlinx.datetime.Instant
import java.security.KeyStore
import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

public actual class TLSIdentity
internal constructor(actual: CBLTLSIdentity) : DelegatedClass<CBLTLSIdentity>(actual) {

    public actual companion object {

        private var keyStore: KeyStore? = null
        private var password: CharArray? = null

        private fun keyStore(): KeyStore =
            requireNotNull(keyStore) { "Initialize JVM KeyStore with TLSIdentity.useKeyStore()" }

        public fun useKeyStore(keyStore: KeyStore, password: CharArray? = null) {
            this.keyStore = keyStore
            this.password = password
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? = CBLTLSIdentity.getIdentity(
            keyStore(),
            alias,
            password
        )?.asTLSIdentity()

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
            keyStore(),
            alias,
            password
        ).asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public fun getIdentity(
            keyStore: KeyStore,
            alias: String,
            password: CharArray?
        ): TLSIdentity? = CBLTLSIdentity.getIdentity(
            keyStore,
            alias,
            password
        )?.asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            keyStore: KeyStore,
            alias: String,
            password: CharArray?
        ): TLSIdentity = CBLTLSIdentity.createIdentity(
            isServer,
            attributes,
            expiration?.toDate(),
            keyStore,
            alias,
            password
        ).asTLSIdentity()
    }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
internal actual val TLSIdentity.actual: CBLTLSIdentity
    get() = actual

internal actual fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
