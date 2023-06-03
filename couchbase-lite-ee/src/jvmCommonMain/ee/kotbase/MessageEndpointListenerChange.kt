package kotbase

import com.couchbase.lite.MessageEndpointListenerChange
import kotbase.base.DelegatedClass

public actual class MessageEndpointListenerChange
internal constructor(actual: com.couchbase.lite.MessageEndpointListenerChange) :
    DelegatedClass<MessageEndpointListenerChange>(actual) {

    public actual val connection: MessageEndpointConnection
        get() = (actual.connection as NativeMessageEndpointConnection).original

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status)
    }
}
