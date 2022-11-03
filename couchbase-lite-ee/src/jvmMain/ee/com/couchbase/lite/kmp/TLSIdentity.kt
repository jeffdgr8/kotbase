package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toDate
import kotlinx.datetime.Instant
import java.security.KeyStore

public actual class TLSIdentity
internal constructor(actual: com.couchbase.lite.TLSIdentity) :
    DelegatedClass<com.couchbase.lite.TLSIdentity>(actual) {

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
        public actual fun getIdentity(alias: String): TLSIdentity? {
            return com.couchbase.lite.TLSIdentity.getIdentity(
                keyStore(),
                alias,
                password
            )?.asTLSIdentity()
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
                keyStore(),
                alias,
                password
            ).asTLSIdentity()
        }

        @Throws(CouchbaseLiteException::class)
        public fun getIdentity(
            keyStore: KeyStore,
            alias: String,
            password: CharArray?
        ): TLSIdentity? {
            return com.couchbase.lite.TLSIdentity.getIdentity(
                keyStore,
                alias,
                password
            )?.asTLSIdentity()
        }

        @Throws(CouchbaseLiteException::class)
        public fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            keyStore: KeyStore,
            alias: String,
            password: CharArray?
        ): TLSIdentity {
            return com.couchbase.lite.TLSIdentity.createIdentity(
                isServer,
                attributes,
                expiration?.toDate(),
                keyStore,
                alias,
                password
            ).asTLSIdentity()
        }
    }
}

internal actual val TLSIdentity.actual: com.couchbase.lite.TLSIdentity
    get() = actual

internal actual fun com.couchbase.lite.TLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
