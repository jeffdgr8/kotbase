package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class MessageEndpointListenerChange
internal constructor(actual: com.couchbase.lite.MessageEndpointListenerChange) :
    DelegatedClass<com.couchbase.lite.MessageEndpointListenerChange>(actual) {

    public actual val connection: MessageEndpointConnection
        get() = (actual.connection as NativeMessageEndpointConnection).original

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status)
    }
}
