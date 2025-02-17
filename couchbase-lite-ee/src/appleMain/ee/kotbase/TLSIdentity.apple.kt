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

import cocoapods.CouchbaseLite.CBLTLSIdentity
import kotbase.internal.DelegatedClass
import kotbase.ext.toByteArray
import kotbase.ext.toKotlinInstantMillis
import kotbase.ext.toNSData
import kotbase.ext.wrapCBLError
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Security.SecCertificateRef
import platform.Security.SecIdentityRef

public actual class TLSIdentity
internal constructor(actual: CBLTLSIdentity) : DelegatedClass<CBLTLSIdentity>(actual) {

    @Suppress("UNCHECKED_CAST")
    public actual val certs: List<ByteArray>
        get() = (actual.certs as List<SecCertificateRef>).map { it.toByteArray() }

    public actual val expiration: Instant
        get() = actual.expiration.toKotlinInstantMillis()

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? {
            return wrapCBLError { error ->
                CBLTLSIdentity.identityWithLabel(alias, error)
            }?.asTLSIdentity()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity {
            return wrapCBLError { error ->
                @Suppress("UNCHECKED_CAST")
                CBLTLSIdentity.createIdentityForServer(
                    isServer,
                    attributes as Map<Any?, *>,
                    expiration?.toNSDate(),
                    alias,
                    error
                )
            }!!.asTLSIdentity()
        }

        /**
         * Get an identity with a SecIdentity object. Any intermediate or root certificates
         * required to identify the certificate but not present in the system-wide set of
         * trusted anchor certificates need to be specified in the optional certs parameter.
         * In addition, the specified SecIdentity object is required to be present in the
         * KeyChain, otherwise an exception will be thrown.
         */
        @Throws(CouchbaseLiteException::class)
        public fun createIdentity(
            identity: SecIdentityRef,
            certs: List<SecCertificateRef>?
        ): TLSIdentity {
            return wrapCBLError { error ->
                CBLTLSIdentity.identityWithIdentity(
                    identity,
                    certs,
                    error
                )
            }!!.asTLSIdentity()
        }

        /**
         * Imports and creates an identity from the given PKCS12 Data. The
         * imported identity will be stored in the Keychain with the given alias.
         */
        @Throws(CouchbaseLiteException::class)
        public fun importIdentity(
            data: ByteArray,
            password: CharArray?,
            alias: String
        ): TLSIdentity {
            return wrapCBLError { error ->
                CBLTLSIdentity.importIdentityWithData(
                    data.toNSData(),
                    password?.concatToString(),
                    alias,
                    error
                )
            }!!.asTLSIdentity()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
            wrapCBLError { error ->
                CBLTLSIdentity.deleteIdentityWithLabel(alias, error)
            }
        }
    }
}

internal fun CBLTLSIdentity.asTLSIdentity(): TLSIdentity =
    TLSIdentity(this)
