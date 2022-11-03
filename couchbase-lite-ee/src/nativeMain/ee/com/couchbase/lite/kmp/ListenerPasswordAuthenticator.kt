package com.couchbase.lite.kmp

public actual class ListenerPasswordAuthenticator
actual constructor(delegate: ListenerPasswordAuthenticatorDelegate) : ListenerAuthenticator {

    init {
        urlEndpointListenerUnsupported()
    }
}
