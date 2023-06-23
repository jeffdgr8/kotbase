package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.MessageEndpointListener as CBLMessageEndpointListener

public actual class MessageEndpointListener
internal constructor(actual: CBLMessageEndpointListener) : DelegatedClass<CBLMessageEndpointListener>(actual) {

    public actual constructor(config: MessageEndpointListenerConfiguration) : this(
        CBLMessageEndpointListener(config.actual)
    )

    public actual fun accept(connection: MessageEndpointConnection) {
        actual.accept(connection.convert())
    }

    public actual fun close(connection: MessageEndpointConnection) {
        actual.close(connection.convert())
    }

    public actual fun closeAll() {
        actual.closeAll()
    }

    public actual fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert())

    public actual fun removeChangeListener(token: ListenerToken) {
        actual.removeChangeListener(token)
    }
}
