@file:JvmName("MessageEndpointListenerChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun MessageEndpointListenerChangeListener.convert(): com.couchbase.lite.MessageEndpointListenerChangeListener {
    return com.couchbase.lite.MessageEndpointListenerChangeListener { change ->
        invoke(MessageEndpointListenerChange(change))
    }
}
