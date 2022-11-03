package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class MessageEndpointListener
internal constructor(actual: com.couchbase.lite.MessageEndpointListener) :
    DelegatedClass<com.couchbase.lite.MessageEndpointListener>(actual) {

    public actual constructor(config: MessageEndpointListenerConfiguration) : this(
        com.couchbase.lite.MessageEndpointListener(config.actual)
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
