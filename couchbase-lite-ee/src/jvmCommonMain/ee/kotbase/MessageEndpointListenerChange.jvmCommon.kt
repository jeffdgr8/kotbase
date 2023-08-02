package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.MessageEndpointListenerChange as CBLMessageEndpointListenerChange

public actual class MessageEndpointListenerChange
internal constructor(actual: CBLMessageEndpointListenerChange) :
    DelegatedClass<CBLMessageEndpointListenerChange>(actual) {

    public actual val connection: MessageEndpointConnection
        get() = (actual.connection as NativeMessageEndpointConnection).original

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status)
    }
}
