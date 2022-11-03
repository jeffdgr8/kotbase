package com.couchbase.lite.kmp

public actual class MessageEndpointListenerChange {

    init {
        messageEndpointUnsupported()
    }

    public actual val connection: MessageEndpointConnection

    public actual val status: ReplicatorStatus
}
