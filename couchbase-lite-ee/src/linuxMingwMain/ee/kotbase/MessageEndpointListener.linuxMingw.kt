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

public actual class MessageEndpointListener
actual constructor(config: MessageEndpointListenerConfiguration) {

    init {
        messageEndpointUnsupported()
    }

    public actual fun accept(connection: MessageEndpointConnection) {
    }

    public actual fun close(connection: MessageEndpointConnection) {
    }

    public actual fun closeAll() {
    }

    public actual fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken =
        messageEndpointUnsupported()

    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: MessageEndpointListenerChangeSuspendListener
    ): ListenerToken = messageEndpointUnsupported()

    public actual fun addChangeListener(scope: CoroutineScope, listener: MessageEndpointListenerChangeSuspendListener) {
        messageEndpointUnsupported()
    }

    @Deprecated(
        "Use ListenerToken.remove()",
        ReplaceWith("token.remove()")
    )
    public actual fun removeChangeListener(token: ListenerToken) {
        token.remove()
    }
}
