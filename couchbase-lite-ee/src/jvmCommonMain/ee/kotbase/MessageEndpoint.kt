package kotbase

import com.couchbase.lite.MessageEndpoint
import kotbase.base.DelegatedClass

public actual class MessageEndpoint
internal constructor(
    override val actual: com.couchbase.lite.MessageEndpoint,
    public actual val delegate: MessageEndpointDelegate
) : DelegatedClass<MessageEndpoint>(actual), Endpoint {

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
