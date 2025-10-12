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

import kotbase.internal.DbContext
import kotbase.internal.fleece.*
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

public actual open class Array
internal constructor(
    actual: FLArray,
    dbContext: DbContext?,
    retain: Boolean = true
) : ArrayInterface, Iterable<Any?> {

    init {
        if (retain) FLArray_Retain(actual)
    }

    private val memory = object {
        val actual = actual
        val retain = retain
    }

    public open val actual: FLArray
        get() = memory.actual

    private val retain: Boolean
        get() = memory.retain

    internal open var dbContext: DbContext? = dbContext
        set(value) {
            field = value
            collectionMap.forEach {
                when (it) {
                    is Array -> it.dbContext = value
                    is Dictionary -> it.dbContext = value
                }
            }
        }

    @OptIn(ExperimentalNativeApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        if (it.retain) FLArray_Release(it.actual)
    }

    internal actual val collectionMap: MutableMap<Int, Any> = mutableMapOf()

    public actual fun toMutable(): MutableArray =
        MutableArray(
            FLArray_MutableCopy(actual, kFLDeepCopy)!!,
            dbContext?.let { DbContext(it.database) }
        )

    actual override val count: Int
        get() = FLArray_Count(actual).toInt()

    protected fun getFLValue(index: Int): FLValue? {
        checkIndex(index)
        return actual.getValue(index)
    }

    actual override fun getValue(index: Int): Any? {
        return collectionMap[index]
            ?: getFLValue(index)?.toNative(dbContext, retain)
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
    }

    actual override fun getString(index: Int): String? =
        getFLValue(index)?.toKString()

    actual override fun getNumber(index: Int): Number? =
        getFLValue(index)?.toNumber()

    actual override fun getInt(index: Int): Int =
        getFLValue(index).toInt()

    actual override fun getLong(index: Int): Long =
        getFLValue(index).toLong()

    actual override fun getFloat(index: Int): Float =
        getFLValue(index).toFloat()

    actual override fun getDouble(index: Int): Double =
        getFLValue(index).toDouble()

    actual override fun getBoolean(index: Int): Boolean =
        getFLValue(index).toBoolean()

    actual override fun getBlob(index: Int): Blob? =
        getFLValue(index)?.toBlob(dbContext, retain)

    actual override fun getDate(index: Int): Instant? =
        getFLValue(index)?.toDate()

    actual override fun getArray(index: Int): Array? {
        return getInternalCollection(index)
            ?: getFLValue(index)?.toArray(dbContext, retain)
                ?.also { collectionMap[index] = it }
    }

    actual override fun getDictionary(index: Int): Dictionary? {
        return getInternalCollection(index)
            ?: getFLValue(index)?.toDictionary(dbContext, retain)
                ?.also { collectionMap[index] = it }
    }

    actual override fun toList(): List<Any?> =
        actual.toList(dbContext)

    actual override fun toJSON(): String =
        FLValue_ToJSON(actual.reinterpret()).toKString()!!

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Array) return false
        if (other.count != count) return false
        val itr1 = iterator()
        val itr2 = other.iterator()
        while (itr1.hasNext() && itr2.hasNext()) {
            val o1 = itr1.next()
            val o2 = itr2.next()
            if (o1 != o2) return false
        }
        return !(itr1.hasNext() || itr2.hasNext())
    }

    override fun hashCode(): Int {
        var result = 1
        for (o in this) {
            result = 31 * result + (o?.hashCode() ?: 0)
        }
        return result
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
