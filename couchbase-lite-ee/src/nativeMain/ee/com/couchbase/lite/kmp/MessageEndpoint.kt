package com.couchbase.lite.kmp

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer

public actual class MessageEndpoint
actual constructor(
    uid: String,
    target: Any?,
    protocolType: ProtocolType,
    delegate: MessageEndpointDelegate
) : Endpoint {

    init {
        messageEndpointUnsupported()
    }

    override val actual: CPointer<CBLEndpoint>

    public actual val uid: String

    public actual val target: Any?

    public actual val protocolType: ProtocolType

    public actual val delegate: MessageEndpointDelegate
}

internal fun messageEndpointUnsupported(): Nothing =
    throw UnsupportedOperationException("Message endpoint is not supported in CBL C SDK")
