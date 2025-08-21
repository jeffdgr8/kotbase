/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import com.couchbase.lite.deleteTLSIdentity
import kotbase.internal.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toDate
import kotbase.ext.toKotlinInstant
import kotlin.time.Instant
import java.security.KeyStore
import com.couchbase.lite.TLSIdentity as CBLTLSIdentity

public actual class TLSIdentity
internal constructor(actual: CBLTLSIdentity) : DelegatedClass<CBLTLSIdentity>(actual) {

    public actual val certs: List<ByteArray>
        get() = actual.certs.map { it.toByteArray() }

    public actual val expiration: Instant
        get() = actual.expiration.toKotlinInstant()

    public actual companion object {

        private var keyStore: KeyStore? = null
        private var keyPassword: CharArray? = null

        private fun keyStore(): KeyStore =
            requireNotNull(keyStore) { "Initialize JVM KeyStore with TLSIdentity.useKeyStore()" }

        /**
         * Register a KeyStore and key password to use in common APIs.
         *
         * @param keyStore KeyStore to use
         * @param keyPassword optional key password to use
         */
        public fun useKeyStore(keyStore: KeyStore, keyPassword: CharArray? = null) {
            this.keyStore = keyStore
            this.keyPassword = keyPassword
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? = CBLTLSIdentity.getIdentity(
            keyStore(),
            alias,
            keyPassword
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
            keyPassword
        ).asTLSIdentity()

        /**
         * Get a TLSIdentity object from the give KeyStore, key alias, and key password.
         * The KeyStore must contain the private key along with the certificate chain at
         * the given key alias and password, otherwise null will be returned.
         *
         * @param keyStore    KeyStore
         * @param alias       key alias
         * @param keyPassword key password if available
         * @return A TLSIdentity object.
         * @throws CouchbaseLiteException on error
         */
        @Throws(CouchbaseLiteException::class)
        public fun getIdentity(
            keyStore: KeyStore,
            alias: String,
            keyPassword: CharArray?
        ): TLSIdentity? = CBLTLSIdentity.getIdentity(
            keyStore,
            alias,
            keyPassword
        )?.asTLSIdentity()

        /**
         * Create a self-signed certificate TLSIdentity object. The generated private key
         * will be stored in the KeyStore along with its self-signed certificate.
         *
         * @param isServer    The flag indicating that the certificate is for server or client.
         * @param attributes  The certificate attributes.
         * @param expiration  The certificate expiration date.
         * @param keyStore    The KeyStore object for storing the generated private key and certificate.
         * @param alias       The key alias for storing the generated private key and certificate.
         * @param keyPassword The password to protect the private key entry in the KeyStore.
         * @return A TLSIdentity object.
         * @throws CouchbaseLiteException on failure
         */
        @Throws(CouchbaseLiteException::class)
        public fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            keyStore: KeyStore,
            alias: String,
            keyPassword: CharArray?
        ): TLSIdentity = CBLTLSIdentity.createIdentity(
            isServer,
            attributes,
            expiration?.toDate(),
            keyStore,
            alias,
            keyPassword
        ).asTLSIdentity()

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
            deleteTLSIdentity(keyStore(), alias)
        }
    }
}

internal actual val TLSIdentity.actual: CBLTLSIdentity
    get() = actual

internal actual fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
