package kotbase

import com.couchbase.lite.MessageEndpoint as CBLMessageEndpoint

public actual class MessageEndpoint
internal constructor(
    actual: CBLMessageEndpoint,
    public actual val delegate: MessageEndpointDelegate
) : Endpoint(actual) {

    public actual constructor(
        uid: String,
        target: Any?,
        protocolType: ProtocolType,
        delegate: MessageEndpointDelegate
    ) : this(
        CBLMessageEndpoint(uid, target, protocolType, delegate.convert()),
        delegate
    )

    public actual val uid: String
        get() = actual.uid

    public actual val target: Any?
        get() = actual.target

    public actual val protocolType: ProtocolType
        get() = actual.protocolType
}

internal val MessageEndpoint.actual: CBLMessageEndpoint
    get() = platformState.actual as CBLMessageEndpoint
