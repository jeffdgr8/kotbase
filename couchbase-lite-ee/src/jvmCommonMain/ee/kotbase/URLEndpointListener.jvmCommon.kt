package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.URLEndpointListener as CBLURLEndpointListener

public actual class URLEndpointListener
internal constructor(
    actual: CBLURLEndpointListener,
    private val _config: URLEndpointListenerConfiguration
) : DelegatedClass<CBLURLEndpointListener>(actual) {

    public actual constructor(config: URLEndpointListenerConfiguration) : this(
        CBLURLEndpointListener(config.actual),
        config
    )

    public actual val config: URLEndpointListenerConfiguration
        get() = URLEndpointListenerConfiguration(_config)

    public actual val port: Int?
        get() {
            val port = actual.port
            return if (port > 0) port else null
        }

    public actual val urls: List<String>
        get() = actual.urls.map { it.toString() }

    public actual val status: ConnectionStatus?
        get() = actual.status?.asConnectionStatus()

    public actual val tlsIdentity: TLSIdentity?
        get() = actual.tlsIdentity?.asTLSIdentity()

    @Throws(CouchbaseLiteException::class)
    public actual fun start() {
        actual.start()
    }

    public actual fun stop() {
        actual.stop()
    }
}
