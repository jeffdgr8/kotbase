package com.couchbase.lite.kmp

public actual class URLEndpointListener
actual constructor(config: URLEndpointListenerConfiguration) {

    init {
        urlEndpointListenerUnsupported()
    }

    public actual val config: URLEndpointListenerConfiguration

    public actual val port: Int

    public actual val urls: List<String>

    public actual val status: ConnectionStatus?

    public actual val tlsIdentity: TLSIdentity?

    @Throws(CouchbaseLiteException::class)
    public actual fun start() {
    }

    public actual fun stop() {
    }
}

internal fun urlEndpointListenerUnsupported(): Nothing =
    throw UnsupportedOperationException("URL endpoint listener is not supported in CBL C SDK")
