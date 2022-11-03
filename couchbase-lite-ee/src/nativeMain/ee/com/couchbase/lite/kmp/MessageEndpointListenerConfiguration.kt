package com.couchbase.lite.kmp

public actual class MessageEndpointListenerConfiguration
actual constructor(
    public actual val database: Database,
    public actual val protocolType: ProtocolType
) {

    init {
        messageEndpointUnsupported()
    }
}
