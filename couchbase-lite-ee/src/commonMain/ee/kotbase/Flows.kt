package kotbase

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * **ENTERPRISE EDITION API**
 *
 * A Flow of message endpoint state changes.
 *
 * @see MessageEndpointListener.addChangeListener
 */
public fun MessageEndpointListener.messageEndpointChangeFlow(): Flow<MessageEndpointListenerChange> = callbackFlow {
    val token = addChangeListener(coroutineContext) { trySend(it) }
    awaitClose { removeChangeListener(token) }
}
