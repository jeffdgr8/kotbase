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
import kotbase.ext.toKotlinInstantMillis
import kotbase.internal.DelegatedClass
import kotlinx.cinterop.convert
import kotlin.time.Instant

public actual open class Array
internal constructor(actual: CBLArray) : DelegatedClass<CBLArray>(actual), ArrayInterface, Iterable<Any?> {

    internal actual val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    actual override val count: Int
        get() = actual.count.toInt()

    actual override fun getValue(index: Int): Any? {
        checkIndex(index)
        return collectionMap[index]
            ?: actual.valueAtIndex(index.convert())?.delegateIfNecessary()
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    actual override fun getString(index: Int): String? {
        checkIndex(index)
        return actual.stringAtIndex(index.convert())
    }

    actual override fun getNumber(index: Int): Number? {
        checkIndex(index)
        return actual.numberAtIndex(index.convert())?.asNumber()
    }

    actual override fun getInt(index: Int): Int {
        checkIndex(index)
        return actual.integerAtIndex(index.convert()).toInt()
    }

    actual override fun getLong(index: Int): Long {
        checkIndex(index)
        return actual.longLongAtIndex(index.convert())
    }

    actual override fun getFloat(index: Int): Float {
        checkIndex(index)
        return actual.floatAtIndex(index.convert())
    }

    actual override fun getDouble(index: Int): Double {
        checkIndex(index)
        return actual.doubleAtIndex(index.convert())
    }

    actual override fun getBoolean(index: Int): Boolean {
        checkIndex(index)
        return actual.booleanAtIndex(index.convert())
    }

    actual override fun getBlob(index: Int): Blob? {
        checkIndex(index)
        return actual.blobAtIndex(index.convert())?.asBlob()
    }

    actual override fun getDate(index: Int): Instant? {
        checkIndex(index)
        return actual.dateAtIndex(index.convert())?.toKotlinInstantMillis()
    }

    actual override fun getArray(index: Int): Array? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.arrayAtIndex(index.convert())?.asArray()
                ?.also { collectionMap[index] = it }
    }

    actual override fun getDictionary(index: Int): Dictionary? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.dictionaryAtIndex(index.convert())?.asDictionary()
                ?.also { collectionMap[index] = it }
    }

    actual override fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    actual override fun toJSON(): String =
        actual.toJSON()

    private var mutations: Long = 0

    protected fun mutate() {
        mutations++
    }

    private val isMutated: Boolean
        get() = mutations > 0

    actual override fun iterator(): Iterator<Any?> =
        ArrayIterator(count, mutations)

    private inner class ArrayIterator(
        private val count: Int,
        private val mutations: Long
    ) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? {
            if (this@Array.mutations != mutations) {
                throw ConcurrentModificationException("Array modified during iteration")
            }
            return getValue(index++)
        }
    }

    override fun toString(): String {
        return buildString {
            append("Array{(")
            append(if (this@Array is MutableArray) '+' else '.')
            append(if (isMutated) '!' else '.')
            append(')')
            val n = count
            for (i in 0..<n) {
                if (i > 0) {
                    append(',')
                }
                append(getValue(i))
            }
            append('}')
        }
    }
}

internal fun CBLArray.asArray() = Array(this)
