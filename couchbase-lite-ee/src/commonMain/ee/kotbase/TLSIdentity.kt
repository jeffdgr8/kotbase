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

import kotlin.time.Instant

/**
 * **ENTERPRISE EDITION API**
 *
 * TLSIdentity provides the identity information obtained from the given KeyStore,
 * including a private key and X.509 certificate chain.  TLSIdentities are backed
 * by the canonical AndroidKeyStore and do not extract private key materials.
 * The TLSIdentity is used by URLEndpointListeners and by Replicator, to set up
 * certificate authenticated TLS communication.
 */
public expect class TLSIdentity {

    public val certs: List<ByteArray>

    public val expiration: Instant

    public companion object {

        /**
         * Get a TLSIdentity backed by the information for the passed alias.
         *
         * @param alias the keystore alias for the identity's entry.
         * @return the identity
         * @throws CouchbaseLiteException on failure to get identity
         */
        @Throws(CouchbaseLiteException::class)
        public fun getIdentity(alias: String): TLSIdentity?

        /**
         * Create self-signed certificate and private key, store them in the canonical keystore,
         * and return an identity backed by the new entry.
         * The identity will be stored in the secure storage using the specified alias
         * and can be recovered using that alias, after this method returns.
         *
         * @param isServer   true if this is a server certificate
         * @param attributes certificate attributes
         * @param expiration expiration date
         * @param alias      alias used to identify the key/certificate entry, in the keystore
         * @return the new identity
         * @throws CouchbaseLiteException on failure to get identity
         */
        @Throws(CouchbaseLiteException::class)
        public fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity

        /**
         * Delete the identity in the keystore with the given alias.
         *
         * @throws CouchbaseLiteException on failure to delete identity
         */
        @Throws(CouchbaseLiteException::class)
        public fun deleteIdentity(alias: String)
    }
}

public val TLSIdentity.Companion.CERT_ATTRIBUTE_COMMON_NAME: String get() = "CN"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_PSEUDONYM: String get() = "pseudonym"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_GIVEN_NAME: String get() = "GN"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_SURNAME: String get() = "SN"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_ORGANIZATION: String get() = "O"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_ORGANIZATION_UNIT: String get() = "OU"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_POSTAL_ADDRESS: String get() = "postalAddress"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_LOCALITY: String get() = "locality"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_POSTAL_CODE: String get() = "postalCode"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_STATE_OR_PROVINCE: String get() = "ST"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_COUNTRY: String get() = "C"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_EMAIL_ADDRESS: String get() = "rfc822Name"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_HOSTNAME: String get() = "dNSName"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_URL: String get() = "uniformResourceIdentifier"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_IP_ADDRESS: String get() = "iPAddress"
public val TLSIdentity.Companion.CERT_ATTRIBUTE_REGISTERED_ID: String get() = "registeredID"
