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

import cnames.structs.CBLListenerAuthenticator
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import libcblite.CBLListenerAuth_Free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual sealed class ListenerAuthenticator(
    actual: CPointer<CBLListenerAuthenticator>,
    ref: StableRef<*>? = null
) {

    private val memory = object {
        val authenticator = actual
        val stableRef = ref
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLListenerAuth_Free(it.authenticator)
        it.stableRef?.dispose()
    }

    internal val actual: CPointer<CBLListenerAuthenticator>
        get() = memory.authenticator
}
