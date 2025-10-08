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

import kotbase.ext.toKotlinInstant
import kotbase.internal.DelegatedClass
import kotlinx.datetime.Instant
import com.couchbase.lite.Array as CBLArray

public actual open class Array
internal constructor(actual: CBLArray) : DelegatedClass<CBLArray>(actual), ArrayInterface, Iterable<Any?> {

    internal actual val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    actual override val count: Int
        get() = actual.count()

    actual override fun getValue(index: Int): Any? {
        return collectionMap[index]
            ?: actual.getValue(index)?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    actual override fun getString(index: Int): String? =
        actual.getString(index)

    actual override fun getNumber(index: Int): Number? =
        actual.getNumber(index)

    actual override fun getInt(index: Int): Int =
        actual.getInt(index)

    actual override fun getLong(index: Int): Long =
        actual.getLong(index)

    actual override fun getFloat(index: Int): Float =
        actual.getFloat(index)

    actual override fun getDouble(index: Int): Double =
        actual.getDouble(index)

    actual override fun getBoolean(index: Int): Boolean =
        actual.getBoolean(index)

    actual override fun getBlob(index: Int): Blob? =
        actual.getBlob(index)?.asBlob()

    actual override fun getDate(index: Int): Instant? =
        actual.getDate(index)?.toKotlinInstant()

    actual override fun getArray(index: Int): Array? {
        return getInternalCollection(index)
            ?: actual.getArray(index)?.asArray()
                ?.also { collectionMap[index] = it }
    }

    actual override fun getDictionary(index: Int): Dictionary? {
        return getInternalCollection(index)
            ?: actual.getDictionary(index)?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    actual override fun toList(): List<Any?> =
        actual.toList().delegateIfNecessary()

    actual override fun toJSON(): String =
        actual.toJSON()

    actual override fun iterator(): Iterator<Any?> = object : Iterator<Any?> {

        private val itr = actual.iterator()

        override fun hasNext(): Boolean = itr.hasNext()

        override fun next(): Any? = itr.next()?.delegateIfNecessary()
    }
}

internal fun CBLArray.asArray() = Array(this)
