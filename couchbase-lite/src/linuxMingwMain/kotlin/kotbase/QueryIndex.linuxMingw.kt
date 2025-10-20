/*
 * Copyright 2024 Jeff Lockhart
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

import cnames.structs.CBLQueryIndex
import kotbase.internal.fleece.toKString
import kotlinx.cinterop.CPointer
import libcblite.CBLQueryIndex_Name
import libcblite.CBLQueryIndex_Release
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual class QueryIndex
internal constructor(
    internal val actual: CPointer<CBLQueryIndex>,
    public actual val collection: Collection
) {

    init {
        debug.RefTracker.trackInit(actual, "CBLQueryIndex")
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        debug.CBLQueryIndex_Release(it)
    }

    public actual val name: String
        get() = CBLQueryIndex_Name(actual).toKString()!!
}

internal fun CPointer<CBLQueryIndex>.asQueryIndex(collection: Collection) = QueryIndex(this, collection)
