package kotbase

import cocoapods.CouchbaseLite.CBLMessageEndpointListenerChange

internal fun MessageEndpointListenerChangeListener.convert(): (CBLMessageEndpointListenerChange?) -> Unit {
    return { change ->
        invoke(MessageEndpointListenerChange(change!!))
    }
}