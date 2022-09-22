package com.couchbase.lite.kmp

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

    public actual fun getValue(index: Int): Any? {
        checkIndex(index)
        return FLArray_Get(actual, index.convert())?.toNative(isMutable)
    }

    public actual fun getString(index: Int): String? =
        getValue(index) as? String

    public actual fun getNumber(index: Int): Number? {
        return when (val value = getValue(index)) {
            is Number -> value
            is Boolean -> if (value) 1 else 0
            else -> null
        }
    }

    public actual fun getInt(index: Int): Int =
        getNumber(index)?.toInt() ?: 0

    public actual fun getLong(index: Int): Long =
        getNumber(index)?.toLong() ?: 0L

    public actual fun getFloat(index: Int): Float =
        getNumber(index)?.toFloat() ?: 0F

    public actual fun getDouble(index: Int): Double =
        getNumber(index)?.toDouble() ?: 0.0

    public actual fun getBoolean(index: Int): Boolean {
        checkIndex(index)
        return FLValue_AsBool(FLArray_Get(actual, index.convert()))
    }

    public actual fun getBlob(index: Int): Blob? =
        getValue(index) as? Blob

    public actual fun getDate(index: Int): Instant? {
        val string = getValue(index) as? String ?: return null
        return try {
            Instant.parse(string)
        } catch (e: Throwable) {
            null
        }
    }

    public actual open fun getArray(index: Int): Array? =
        getValue(index) as? Array

    public actual open fun getDictionary(index: Int): Dictionary? =
        getValue(index) as? Dictionary

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
