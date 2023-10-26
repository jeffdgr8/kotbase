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

import cocoapods.CouchbaseLite.CBLArray
import kotbase.ext.asNumber
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual open class Array
internal constructor(actual: CBLArray) : DelegatedClass<CBLArray>(actual), Iterable<Any?> {

    internal actual val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    public actual fun getValue(index: Int): Any? {
        checkIndex(index)
        return collectionMap[index]
            ?: actual.valueAtIndex(index.convert())?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    public actual fun getString(index: Int): String? {
        checkIndex(index)
        return actual.stringAtIndex(index.convert())
    }

    public actual fun getNumber(index: Int): Number? {
        checkIndex(index)
        return actual.numberAtIndex(index.convert())?.asNumber()
    }

    public actual fun getInt(index: Int): Int {
        checkIndex(index)
        return actual.integerAtIndex(index.convert()).toInt()
    }

    public actual fun getLong(index: Int): Long {
        checkIndex(index)
        return actual.longLongAtIndex(index.convert())
    }

    public actual fun getFloat(index: Int): Float {
        checkIndex(index)
        return actual.floatAtIndex(index.convert())
    }

    public actual fun getDouble(index: Int): Double {
        checkIndex(index)
        return actual.doubleAtIndex(index.convert())
    }

    public actual fun getBoolean(index: Int): Boolean {
        checkIndex(index)
        return actual.booleanAtIndex(index.convert())
    }

    public actual fun getBlob(index: Int): Blob? {
        checkIndex(index)
        return actual.blobAtIndex(index.convert())?.asBlob()
    }

    public actual fun getDate(index: Int): Instant? {
        checkIndex(index)
        return actual.dateAtIndex(index.convert())?.toKotlinInstant()
    }

    public actual open fun getArray(index: Int): Array? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.arrayAtIndex(index.convert())?.asArray()
                ?.also { collectionMap[index] = it }
    }

    public actual open fun getDictionary(index: Int): Dictionary? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.dictionaryAtIndex(index.convert())?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    public actual fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    public actual open fun toJSON(): String =
        actual.toJSON()

    actual override operator fun iterator(): Iterator<Any?> =
        ArrayIterator(count)

    private inner class ArrayIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? = getValue(index++)
    }
}

internal fun CBLArray.asArray() = Array(this)
