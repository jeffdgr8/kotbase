package kotbase

import kotlinx.datetime.Instant

public actual class TLSIdentity {

    public actual val certs: List<ByteArray>
        get() = emptyList()

    public actual val expiration: Instant
        get() = urlEndpointListenerUnsupported()

    public actual companion object {

        @Throws(CouchbaseLiteException::class)
        public actual fun getIdentity(alias: String): TLSIdentity? = null

        @Throws(CouchbaseLiteException::class)
        public actual fun createIdentity(
            isServer: Boolean,
            attributes: Map<String, String>,
            expiration: Instant?,
            alias: String
        ): TLSIdentity {
            urlEndpointListenerUnsupported()
        }

        @Throws(CouchbaseLiteException::class)
        public actual fun deleteIdentity(alias: String) {
        }
    }
}
