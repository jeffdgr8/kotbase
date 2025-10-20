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

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer
import libcblite.CBLEndpoint_Free
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual sealed class Endpoint(internal val actual: CPointer<CBLEndpoint>) {

    init {
        debug.RefTracker.trackInit(actual, "CBLEndpoint")
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        debug.CBLEndpoint_Free(it)
    }
}
