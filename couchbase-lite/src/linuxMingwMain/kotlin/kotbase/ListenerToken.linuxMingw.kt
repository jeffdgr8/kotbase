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
import kotlinx.atomicfu.atomic
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import libcblite.CBLListener_Remove

public actual sealed class ListenerToken : AutoCloseable {

    actual override fun close() {
        removeImpl()
    }

    public actual fun remove() {
        removeImpl()
    }

    protected abstract fun removeImpl()
}

internal class SuspendListenerToken(
    private val scope: CoroutineScope,
    private  val token: ListenerToken
) : ListenerToken() {

    override fun removeImpl() {
        token.remove()
        scope.cancel()
    }
}

internal class StableRefListenerToken<T : Any>(
    ref: T,
    actualBuilder: (COpaquePointer) -> CPointer<CBLListenerToken>
) : ListenerToken() {

    private val stableRef = StableRef.create(ref)

    internal val actual: CPointer<CBLListenerToken> = actualBuilder(stableRef.asCPointer())

    private val disposed = atomic(false)

    override fun removeImpl() {
        if (disposed.compareAndSet(expect = false, update = true)) {
            CBLListener_Remove(actual)
            stableRef.dispose()
        }
    }
}
