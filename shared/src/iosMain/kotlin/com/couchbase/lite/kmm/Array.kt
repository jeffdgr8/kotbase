package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLArray
import com.udobny.kmm.DelegatedClass
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual open class Array
internal constructor(actual: CBLArray) :
    DelegatedClass<CBLArray>(actual), Iterable<Any?> {

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    public actual fun getValue(index: Int): Any? =
        actual.valueAtIndex(index.convert())

    public actual fun getString(index: Int): String? =
        actual.stringAtIndex(index.convert())

    public actual fun getNumber(index: Int): Number? =
        actual.numberAtIndex(index.convert()) as Number?

    public actual fun getInt(index: Int): Int =
        actual.integerAtIndex(index.convert()).toInt()

    public actual fun getLong(index: Int): Long =
        actual.longLongAtIndex(index.convert())

    public actual fun getFloat(index: Int): Float =
        actual.floatAtIndex(index.convert())

    public actual fun getDouble(index: Int): Double =
        actual.doubleAtIndex(index.convert())

    public actual fun getBoolean(index: Int): Boolean =
        actual.booleanAtIndex(index.convert())

    public actual fun getBlob(index: Int): Blob? =
        actual.blobAtIndex(index.convert())?.asBlob()

    public actual fun getDate(index: Int): Instant? =
        actual.dateAtIndex(index.convert())?.toKotlinInstant()

    public actual open fun getArray(index: Int): Array? =
        actual.arrayAtIndex(index.convert())?.asArray()

    public actual open fun getDictionary(index: Int): Dictionary? =
        actual.dictionaryAtIndex(index.convert())?.asDictionary()

    public actual fun toList(): List<Any?> =
        actual.toArray()

    public actual fun toJSON(): String =
        actual.toJSON()

    override operator fun iterator(): Iterator<Any?> =
        ArrayIterator(count())

    private inner class ArrayIterator(private val count: Int) : Iterator<Any?> {

        private var index = 0

        override fun hasNext(): Boolean {
            return index < count
        }

        override fun next(): Any? {
            return getValue(index++)
        }
    }
}

internal fun CBLArray.asArray() = Array(this)
