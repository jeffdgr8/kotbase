package kotbase

import cocoapods.CouchbaseLite.CBLURLEndpointListener
import kotbase.base.DelegatedClass
import kotbase.ext.wrapCBLError
import kotlinx.cinterop.useContents
import platform.Foundation.NSURL

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
            val port = actual.port.toInt()
            return if (port > 0) port else null
        }

    public actual val urls: List<String>
        get() = actual.urls?.map { (it as NSURL).path!! } ?: emptyList()

    public actual val status: ConnectionStatus?
        get() {
            return actual.status.useContents {
                ConnectionStatus(connectionCount.toInt(), activeConnectionCount.toInt())
            }
        }

    public actual val tlsIdentity: TLSIdentity?
        get() = actual.tlsIdentity?.asTLSIdentity()

    @Throws(CouchbaseLiteException::class)
    public actual fun start() {
        wrapCBLError { error ->
            actual.startWithError(error)
        }
    }

    public actual fun stop() {
        actual.stop()
    }
}
