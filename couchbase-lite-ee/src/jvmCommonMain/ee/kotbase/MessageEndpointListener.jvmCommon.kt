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

import kotbase.base.DelegatedClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import com.couchbase.lite.MessageEndpointListener as CBLMessageEndpointListener

public actual class MessageEndpointListener
internal constructor(actual: CBLMessageEndpointListener) : DelegatedClass<CBLMessageEndpointListener>(actual) {

    public actual constructor(config: MessageEndpointListenerConfiguration) : this(
        CBLMessageEndpointListener(config.actual)
    )

    public actual fun accept(connection: MessageEndpointConnection) {
        actual.accept(connection.convert())
    }

    public actual fun close(connection: MessageEndpointConnection) {
        actual.close(connection.convert())
    }

    public actual fun closeAll() {
        actual.closeAll()
    }

    public actual fun addChangeListener(listener: MessageEndpointListenerChangeListener): ListenerToken =
        actual.addChangeListener(listener.convert())

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(
        context: CoroutineContext,
        listener: MessageEndpointListenerChangeSuspendListener
    ): ListenerToken {
        val scope = CoroutineScope(SupervisorJob() + context)
        val token = actual.addChangeListener(context[CoroutineDispatcher]?.asExecutor(), listener.convert(scope))
        return SuspendListenerToken(scope, token)
    }

    @OptIn(ExperimentalStdlibApi::class)
    public actual fun addChangeListener(scope: CoroutineScope, listener: MessageEndpointListenerChangeSuspendListener) {
        val token = actual.addChangeListener(
            scope.coroutineContext[CoroutineDispatcher]?.asExecutor(),
            listener.convert(scope)
        )
        scope.coroutineContext[Job]?.invokeOnCompletion {
            actual.removeChangeListener(token)
        }
    }

    public actual fun removeChangeListener(token: ListenerToken) {
        if (token is SuspendListenerToken) {
            actual.removeChangeListener(token.actual)
            token.scope.cancel()
        } else {
            actual.removeChangeListener(token)
        }
    }
}
