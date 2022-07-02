package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toKotlinInstant
import kotlinx.datetime.Instant

public actual class Result
internal constructor(actual: com.couchbase.lite.Result) :
    DelegatedClass<com.couchbase.lite.Result>(actual), Iterable<String> {

    public actual val count: Int
        get() = actual.count()

    public actual fun getValue(index: Int): Any? =
        actual.getValue(index)?.delegateIfNecessary()

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
        actual.getBlob(index)?.asBlob()

    public actual fun getDate(index: Int): Instant? =
        actual.getDate(index)?.toKotlinInstant()

    public actual fun getArray(index: Int): Array? =
        actual.getArray(index)?.asArray()

    public actual fun getDictionary(index: Int): Dictionary? =
        actual.getDictionary(index)?.asDictionary()

    public actual fun toList(): List<Any?> =
        actual.toList().delegateIfNecessary()

    public actual val keys: List<String>
        get() = actual.keys

    public actual fun getValue(key: String): Any? =
        actual.getValue(key)?.delegateIfNecessary()

    public actual fun getString(key: String): String? =
        actual.getString(key)

    public actual fun getNumber(key: String): Number? =
        actual.getNumber(key)

    public actual fun getInt(key: String): Int =
        actual.getInt(key)

    public actual fun getLong(key: String): Long =
        actual.getLong(key)

    public actual fun getFloat(key: String): Float =
        actual.getFloat(key)

    public actual fun getDouble(key: String): Double =
        actual.getDouble(key)

    public actual fun getBoolean(key: String): Boolean =
        actual.getBoolean(key)

    public actual fun getBlob(key: String): Blob? =
        actual.getBlob(key)?.asBlob()

    public actual fun getDate(key: String): Instant? =
        actual.getDate(key)?.toKotlinInstant()

    public actual fun getArray(key: String): Array? =
        actual.getArray(key)?.asArray()

    public actual fun getDictionary(key: String): Dictionary? =
        actual.getDictionary(key)?.asDictionary()

    public actual fun toMap(): Map<String, Any?> =
        actual.toMap().delegateIfNecessary()

    public actual fun toJSON(): String =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        actual.contains(key)

    actual override operator fun iterator(): Iterator<String> =
        actual.iterator()
}

internal fun com.couchbase.lite.Result.asResult() = Result(this)
