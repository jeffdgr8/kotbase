package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDictionary
import com.udobny.kmm.DelegatedClass
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual open class Dictionary
internal constructor(actual: CBLDictionary) :
    DelegatedClass<CBLDictionary>(actual), Iterable<String> {

    public actual fun toMutable(): MutableDictionary =
        MutableDictionary(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    @Suppress("UNCHECKED_CAST")
    public actual val keys: List<String>
        get() = actual.keys as List<String>

    public actual fun getValue(key: String): Any? =
        actual.valueForKey(key)

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

    public actual open fun getArray(key: String): Array? =
        actual.arrayForKey(key)?.asArray()

    public actual open fun getDictionary(key: String): Dictionary? =
        actual.dictionaryForKey(key)?.asDictionary()

    @Suppress("UNCHECKED_CAST")
    public actual fun toMap(): Map<String, Any?> =
        actual.toDictionary() as Map<String, Any?>

    public actual fun toJSON(): String =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        actual.containsValueForKey(key)

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<String> =
        (actual.keys as List<String>).iterator()
}

internal fun CBLDictionary.asDictionary() = Dictionary(this)
