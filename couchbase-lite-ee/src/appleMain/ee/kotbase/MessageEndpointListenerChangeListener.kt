package kotbase

import cocoapods.CouchbaseLite.CBLMessageEndpointListenerChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun MessageEndpointListenerChangeListener.convert(): (CBLMessageEndpointListenerChange?) -> Unit {
    return { change ->
        invoke(MessageEndpointListenerChange(change!!))
    }
}

internal fun MessageEndpointListenerChangeSuspendListener.convert(
    scope: CoroutineScope
): (CBLMessageEndpointListenerChange?) -> Unit {
    return { change ->
        scope.launch {
            invoke(MessageEndpointListenerChange(change!!))
        }
    }
}
