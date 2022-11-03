package com.couchbase.lite.kmp

public actual class Message {

    init {
        messageEndpointUnsupported()
    }

    public actual fun toData(): ByteArray =
        messageEndpointUnsupported()

    public actual companion object {

        public actual fun fromData(data: ByteArray): Message {
            messageEndpointUnsupported()
        }
    }
}
