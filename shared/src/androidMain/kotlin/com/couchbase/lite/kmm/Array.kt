package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toKotlinInstant
import kotlinx.datetime.Instant

public actual open class Array
internal constructor(actual: com.couchbase.lite.Array) :
    DelegatedClass<com.couchbase.lite.Array>(actual), Iterable<Any?> {

    public actual fun toMutable(): MutableArray =
        MutableArray(actual.toMutable())

    public actual val count: Int
        get() = actual.count()

    public actual fun getValue(index: Int): Any? =
        actual.getValue(index)

    public actual fun getString(index: Int): String? =
        actual.getString(index)

    public actual fun getNumber(index: Int): Number? =
        actual.getNumber(index)

    public actual fun getInt(index: Int): Int =
        actual.getInt(index)

    public actual fun getLong(index: Int): Long =
        actual.getLong(index)

    public actual fun getFloat(index: Int): Float =
        actual.getFloat(index)

    public actual fun getDouble(index: Int): Double =
        actual.getDouble(index)

    public actual fun getBoolean(index: Int): Boolean =
        actual.getBoolean(index)

    public actual fun getBlob(index: Int): Blob? =
        actual.getBlob(index)

    public actual fun getDate(index: Int): Instant? =
        actual.getDate(index)?.toKotlinInstant()

    public actual open fun getArray(index: Int): Array? =
        actual.getArray(index)?.asArray()

    public actual open fun getDictionary(index: Int): Dictionary? =
        actual.getDictionary(index)?.asDictionary()

    public actual fun toList(): List<Any?> =
        actual.toList()

    public actual fun toJSON(): String =
        actual.toJSON()

    override operator fun iterator(): Iterator<Any?> =
        actual.iterator()
}

internal fun com.couchbase.lite.Array.asArray() = Array(this)
