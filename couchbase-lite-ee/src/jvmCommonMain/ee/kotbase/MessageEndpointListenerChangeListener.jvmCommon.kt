@file:JvmName("MessageEndpointListenerChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.MessageEndpointListenerChangeListener as CBLMessageEndpointListenerChangeListener

internal fun MessageEndpointListenerChangeListener.convert(): CBLMessageEndpointListenerChangeListener {
    return CBLMessageEndpointListenerChangeListener { change ->
        invoke(MessageEndpointListenerChange(change))
    }
}

internal fun MessageEndpointListenerChangeSuspendListener.convert(
    scope: CoroutineScope
): CBLMessageEndpointListenerChangeListener {
    return CBLMessageEndpointListenerChangeListener { change ->
        scope.launch {
            invoke(MessageEndpointListenerChange(change))
        }
    }
}
