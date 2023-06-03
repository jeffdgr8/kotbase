@file:JvmName("MessageEndpointListenerChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

internal fun MessageEndpointListenerChangeListener.convert(): com.couchbase.lite.MessageEndpointListenerChangeListener {
    return com.couchbase.lite.MessageEndpointListenerChangeListener { change ->
        invoke(MessageEndpointListenerChange(change))
    }
}
