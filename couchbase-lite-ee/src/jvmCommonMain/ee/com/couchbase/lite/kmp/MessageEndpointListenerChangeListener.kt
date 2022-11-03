package com.couchbase.lite.kmp

internal fun MessageEndpointListenerChangeListener.convert(): com.couchbase.lite.MessageEndpointListenerChangeListener {
    return com.couchbase.lite.MessageEndpointListenerChangeListener { change ->
        invoke(MessageEndpointListenerChange(change))
    }
}
