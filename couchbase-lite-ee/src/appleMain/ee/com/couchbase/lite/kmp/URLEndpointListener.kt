package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLURLEndpointListener
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.DelegatedClass
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

    public actual val port: Int
        get() = actual.port.toInt()

    public actual val urls: List<String>
        get() = actual.urls!!.map { (it as NSURL).path!! }

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
