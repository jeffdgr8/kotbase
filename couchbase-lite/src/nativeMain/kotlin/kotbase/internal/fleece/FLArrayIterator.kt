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
package kotbase.internal.fleece

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import libcblite.*

internal fun FLArray.iterator(memScope: MemScope): Iterator<FLValue> =
    FLArrayKIterator(this, memScope)

private class FLArrayKIterator(array: FLArray, memScope: MemScope) : Iterator<FLValue> {

    private val itr = memScope.alloc<FLArrayIterator>()

    init {
        FLArrayIterator_Begin(array, itr.ptr)
    }

    override fun hasNext(): Boolean =
        FLArrayIterator_GetValue(itr.ptr) != null

    override fun next(): FLValue {
        val value = FLArrayIterator_GetValue(itr.ptr) ?: throw NoSuchElementException()
        FLArrayIterator_Next(itr.ptr)
        return value
    }
}
