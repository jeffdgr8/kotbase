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

import cnames.structs.CBLResultSet
import kotbase.internal.DbContext
import kotlinx.cinterop.CPointer
import libcblite.CBLResultSet_Next
import libcblite.CBLResultSet_Release
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalStdlibApi::class)
public actual class ResultSet
internal constructor(
    actual: CPointer<CBLResultSet>,
    private val dbContext: DbContext? = null
) : Iterable<Result>, AutoCloseable {

    private val memory = object {
        var closeCalled = false
        val actual = actual
    }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (!it.closeCalled) {
            CBLResultSet_Release(it.actual)
        }
    }

    internal val actual: CPointer<CBLResultSet>
        get() = memory.actual

    public actual operator fun next(): Result? {
        return if (CBLResultSet_Next(actual)) {
            Result(actual, dbContext)
        } else null
    }

    public actual fun allResults(): List<Result> {
        val results = mutableListOf<Result>()
        while (true) {
            val result = next() ?: break
            results.add(result)
        }
        return results
    }

    actual override fun iterator(): Iterator<Result> =
        allResults().iterator()

    actual override fun close() {
        memory.closeCalled = true
        CBLResultSet_Release(actual)
    }
}

internal fun CPointer<CBLResultSet>.asResultSet() = ResultSet(this)
