package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class MessageEndpoint
internal constructor(
    override val actual: com.couchbase.lite.MessageEndpoint,
    public actual val delegate: MessageEndpointDelegate
) : DelegatedClass<com.couchbase.lite.MessageEndpoint>(actual), Endpoint {

    public actual constructor(
        uid: String,
        target: Any?,
        protocolType: ProtocolType,
        delegate: MessageEndpointDelegate
    ) : this(
        com.couchbase.lite.MessageEndpoint(uid, target, protocolType, delegate.convert()),
        delegate
    )

    public actual val uid: String
        get() = actual.uid

    public actual val target: Any?
        get() = actual.target

    public actual val protocolType: ProtocolType
        get() = actual.protocolType
}
