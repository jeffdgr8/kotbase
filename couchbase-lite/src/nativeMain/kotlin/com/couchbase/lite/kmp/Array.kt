package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.getValue
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.fleece.toList
import com.couchbase.lite.kmp.internal.fleece.toNative
import libcblite.*
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import kotlin.native.internal.createCleaner

public actual open class Array
internal constructor(internal open val actual: FLArray) : Iterable<Any?> {

    init {
        FLArray_Retain(actual)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        FLArray_Release(it)
    }

    protected open val isMutable: Boolean = false

    public actual fun toMutable(): MutableArray =
        MutableArray(FLArray_AsMutable(actual) ?: FLArray_MutableCopy(actual, kFLDeepCopy)!!)

    public actual val count: Int
        get() = FLArray_Count(actual).toInt()

    private fun getFLValue(index: Int): FLValue? {
        checkIndex(index)
        return actual.getValue(index)
    }

    public actual fun getValue(index: Int): Any? =
        getFLValue(index)?.toNative(isMutable)

    public actual fun getString(index: Int): String? =
        getFLValue(index)?.toKString()

    public actual fun getNumber(index: Int): Number? =
        getFLValue(index)?.toNumber()

    public actual fun getInt(index: Int): Int =
        getFLValue(index).toInt()

    public actual fun getLong(index: Int): Long =
        getFLValue(index).toLong()

    public actual fun getFloat(index: Int): Float =
        getFLValue(index).toFloat()

    public actual fun getDouble(index: Int): Double =
        getFLValue(index).toDouble()

    public actual fun getBoolean(index: Int): Boolean =
        getFLValue(index).toBoolean()

    public actual fun getBlob(index: Int): Blob? =
        getFLValue(index)?.toBlob()

    public actual fun getDate(index: Int): Instant? =
        getFLValue(index)?.toDate()

    public actual open fun getArray(index: Int): Array? =
        getFLValue(index)?.toArray(isMutable)

    public actual open fun getDictionary(index: Int): Dictionary? =
        getFLValue(index)?.toDictionary(isMutable)

    public actual fun toList(): List<Any?> =
        actual.toList()

    public actual open fun toJSON(): String =
        FLValue_ToJSON(actual.reinterpret()).toKString()!!

    override operator fun iterator(): Iterator<Any?> =
        ArrayIterator(count)

    private inner class ArrayIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean = index < count

        override fun next(): Any? = getValue(index++)
    }

    protected fun checkIndex(index: Int) {
        if (index < 0 || index >= count) {
            throw IndexOutOfBoundsException("Array index $index is out of range")
        }
    }
}

internal fun FLArray.asArray() = Array(this)
