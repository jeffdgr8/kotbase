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

internal fun FLDict.keyValueIterator(memScope: MemScope): Iterator<Pair<String, FLValue>> =
    FLDictKeyValueIterator(this, memScope)

private class FLDictKeyValueIterator(
    dict: FLDict,
    memScope: MemScope
) : Iterator<Pair<String, FLValue>> {

    private val itr = memScope.alloc<FLDictIterator>()

    init {
        FLDictIterator_Begin(dict, itr.ptr)
    }

    override fun hasNext(): Boolean =
        FLDictIterator_GetValue(itr.ptr) != null

    override fun next(): Pair<String, FLValue> {
        val key = FLDictIterator_GetKeyString(itr.ptr).toKString() ?: throw NoSuchElementException()
        val value = FLDictIterator_GetValue(itr.ptr)!!
        FLDictIterator_Next(itr.ptr)
        return Pair(key, value)
    }
}

internal fun FLDict.keyIterator(memScope: MemScope): Iterator<String> =
    FLDictKeyIterator(this, memScope)

private class FLDictKeyIterator(
    dict: FLDict,
    memScope: MemScope
) : Iterator<String> {

    private val itr = memScope.alloc<FLDictIterator>()

    init {
        FLDictIterator_Begin(dict, itr.ptr)
    }

    override fun hasNext(): Boolean =
        FLDictIterator_GetValue(itr.ptr) != null

    override fun next(): String {
        val key = FLDictIterator_GetKeyString(itr.ptr).toKString() ?: throw NoSuchElementException()
        FLDictIterator_Next(itr.ptr)
        return key
    }
}
