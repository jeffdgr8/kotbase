package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

public actual class MessageEndpointListener
actual constructor(config: MessageEndpointListenerConfiguration) {

    init {
        messageEndpointUnsupported()
    }

    public actual fun accept(connection: MessageEndpointConnection) {
    }

    public actual fun close(connection: MessageEndpointConnection) {
    }

    public actual fun closeAll() {
    }

    public actual fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken =
        messageEndpointUnsupported()

    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: MessageEndpointListenerChangeSuspendListener
    ): ListenerToken = messageEndpointUnsupported()

    public actual fun addChangeListener(scope: CoroutineScope, listener: MessageEndpointListenerChangeSuspendListener) {
        messageEndpointUnsupported()
    }

    public actual fun removeChangeListener(token: ListenerToken) {
    }
}
