package com.couchbase.lite.kmp

import kotlinx.datetime.Instant

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

    public companion object {

        /**
         * Get a TLSIdentity backed by the information for the passed alias.
         *
         * @param alias the keystore alias for the identities entry.
         * @return the identity
         * @throws CouchbaseLiteException on failure to get identity
         */
        @Throws(CouchbaseLiteException::class)
        public fun getIdentity(alias: String): TLSIdentity?

        /**
         * Create self-signed certificate and private key, store them in the canonical keystore,
         * and return a identity backed by the new entry.
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
    }
}