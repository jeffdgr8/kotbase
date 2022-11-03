package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMessageEndpoint
import com.udobny.kmp.DelegatedClass

public actual class MessageEndpoint
internal constructor(
    override val actual: CBLMessageEndpoint,
    public actual val delegate: MessageEndpointDelegate
) : DelegatedClass<CBLMessageEndpoint>(actual), Endpoint {

    public actual constructor(
        uid: String,
        target: Any?,
        protocolType: ProtocolType,
        delegate: MessageEndpointDelegate
    ) : this(
        CBLMessageEndpoint(uid, target, protocolType.actual, delegate.convert(delegate)),
        delegate
    )

    public actual val uid: String
        get() = actual.uid

    public actual val target: Any?
        get() = actual.target

    public actual val protocolType: ProtocolType
        get() = ProtocolType.from(actual.protocolType)
}
