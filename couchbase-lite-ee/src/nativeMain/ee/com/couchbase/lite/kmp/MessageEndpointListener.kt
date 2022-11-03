package com.couchbase.lite.kmp

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

    public actual fun removeChangeListener(token: ListenerToken) {
    }
}
