package com.couchbase.lite.kmp

import kotlinx.datetime.Instant

public actual class TLSIdentity {

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? {
            urlEndpointListenerUnsupported()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity {
            urlEndpointListenerUnsupported()
        }
    }
}
