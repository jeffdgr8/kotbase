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

import cnames.structs.CBLListenerToken
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.CoroutineScope

public actual interface ListenerToken

internal class DelegatedListenerToken(
    val actual: CPointer<CBLListenerToken>,
    val type: ListenerTokenType,
    val index: Int
) : ListenerToken

internal class SuspendListenerToken(
    val scope: CoroutineScope,
    val token: DelegatedListenerToken
) : ListenerToken

internal enum class ListenerTokenType {
    DATABASE,
    DOCUMENT,
    QUERY,
    REPLICATOR,
    DOCUMENT_REPLICATION
}

internal fun <T : Any> addListener(
    listeners: MutableList<StableRef<T>?>,
    listener: T
): Pair<Int, COpaquePointer> {
    val stableRef = StableRef.create(listener)
    var index = listeners.indexOf(null)
    if (index < 0) {
        listeners.add(stableRef)
        index = listeners.lastIndex
    } else {
        listeners[index] = stableRef
    }
    return Pair(index, stableRef.asCPointer())
}

internal fun <T : Any> removeListener(listeners: MutableList<StableRef<T>?>, index: Int) {
    if (index > listeners.lastIndex) return
    val stableRef = listeners[index] ?: return
    stableRef.dispose()
    listeners[index] = null
    while (listeners.isNotEmpty() && listeners.last() == null) {
        listeners.removeLast()
    }
}
