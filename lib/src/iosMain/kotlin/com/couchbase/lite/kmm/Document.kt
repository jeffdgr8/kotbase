package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDictionary
import cocoapods.CouchbaseLite.CBLDocument
import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.asNumber
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant

public actual open class Document
internal constructor(actual: CBLDocument) :
    DelegatedClass<CBLDocument>(actual), Iterable<String> {

    public actual val id: String
        get() = actual.id

    public actual val revisionID: String?
        get() = actual.revisionID

    public actual val sequence: Long
        get() = actual.sequence.toLong()

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual val count: Int
        get() = actual.count.toInt()

    @Suppress("UNCHECKED_CAST")
    public actual val keys: List<String>
        get() = actual.keys as List<String>

    public actual fun getValue(key: String): Any? =
        actual.valueForKey(key)?.delegateIfNecessary()

    public actual fun getString(key: String): String? =
        actual.stringForKey(key)

    public actual fun getNumber(key: String): Number? =
        actual.numberForKey(key)?.asNumber()

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
        actual.toDictionary().delegateIfNecessary() as Map<String, Any?>

    public actual open fun toJSON(): String? =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        // iOS SDK implements as actual.booleanForKey(key), this will behave like Java SDK
        actual.toDictionary().containsKey(key)

    @Suppress("UNCHECKED_CAST")
    actual override operator fun iterator(): Iterator<String> =
        (actual.keys as List<String>).iterator()
}

internal fun CBLDocument.asDocument() = Document(this)