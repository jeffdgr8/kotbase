/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * **ENTERPRISE EDITION API**
 *
 * MessageEndpointListener to serve incoming message endpoint connection.
 */
public expect class MessageEndpointListener(config: MessageEndpointListenerConfiguration) {

    /**
     * Accept a new connection.
     *
     * @param connection new incoming connection
     */
    public fun accept(connection: MessageEndpointConnection)

    /**
     * Close the given connection.
     *
     * @param connection the connection to be closed
     */
    public fun close(connection: MessageEndpointConnection)

    /**
     * Close all connections active at the time of the call.
     */
    public fun closeAll()

    /**
     * Add a change listener.
     *
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken

    /**
     * Add a change listener with a [CoroutineContext] that will be used to launch coroutines the listener will be
     * called on. Coroutines will be launched in a [CoroutineScope] that is canceled when the listener is removed.
     *
     * @param context coroutine context in which the listener will run
     * @param listener The listener to post changes.
     * @return An opaque listener token object for removing the listener.
     *
     * @see ListenerToken.remove
     */
    public fun addChangeListener(
        context: CoroutineContext,
        listener: MessageEndpointListenerChangeSuspendListener
    ): ListenerToken

    /**
     * Add a change listener with a [CoroutineScope] that will be used to launch coroutines the listener will be
     * called on. The listener is removed when the scope is canceled.
     *
     * @param scope coroutine scope in which the listener will run
     * @param listener The listener to post changes.
     */
    public fun addChangeListener(scope: CoroutineScope, listener: MessageEndpointListenerChangeSuspendListener)

    /**
     * Remove a change listener.
     *
     * @param token identifier for the listener to be removed
     */
    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    public fun removeChangeListener(token: ListenerToken)
}
