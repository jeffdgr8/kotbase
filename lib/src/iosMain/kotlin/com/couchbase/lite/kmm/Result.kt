package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryResult
import com.udobny.kmm.DelegatedClass
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual class Result
internal constructor(actual: CBLQueryResult) :
    DelegatedClass<CBLQueryResult>(actual), Iterable<String> {

    public actual val count: Int
        get() = actual.count().toInt()

    public actual fun getValue(index: Int): Any? =
        actual.valueAtIndex(index.convert())?.delegateIfNecessary()

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

    public actual fun getArray(index: Int): Array? =
        actual.arrayAtIndex(index.convert())?.asArray()

    public actual fun getDictionary(index: Int): Dictionary? =
        actual.dictionaryAtIndex(index.convert())?.asDictionary()

    public actual fun toList(): List<Any?> =
        actual.toArray().delegateIfNecessary()

    @Suppress("UNCHECKED_CAST")
    public actual val keys: List<String>
        get() = actual.keys as List<String>

    public actual fun getValue(key: String): Any? =
        actual.valueForKey(key)?.delegateIfNecessary()

    public actual fun getString(key: String): String? =
        actual.stringForKey(key)

    public actual fun getNumber(key: String): Number? =
        actual.numberForKey(key) as Number?

    public actual fun getInt(key: String): Int =
        actual.integerForKey(key).toInt()

    public actual fun getLong(key: String): Long =
        actual.longLongForKey(key)

    public actual fun getFloat(key: String): Float =
        actual.floatForKey(key)

    public actual fun getDouble(key: String): Double =
        actual.doubleForKey(key)

    public actual fun getBoolean(key: String): Boolean =
        actual.booleanForKey(key)

    public actual fun getBlob(key: String): Blob? =
        actual.blobForKey(key)?.asBlob()

    public actual fun getDate(key: String): Instant? =
        actual.dateForKey(key)?.toKotlinInstant()

    public actual fun getArray(key: String): Array? =
        actual.arrayForKey(key)?.asArray()

    public actual fun getDictionary(key: String): Dictionary? =
        actual.dictionaryForKey(key)?.asDictionary()

    @Suppress("UNCHECKED_CAST")
    public actual fun toMap(): Map<String, Any?> =
        actual.toDictionary().delegateIfNecessary() as Map<String, Any?>

    public actual fun toJSON(): String =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        actual.containsValueForKey(key)

    @Suppress("UNCHECKED_CAST")
    actual override fun iterator(): Iterator<String> =
        (actual.keys as List<String>).iterator()
}

internal fun CBLQueryResult.asResult() = Result(this)