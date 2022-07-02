package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.ext.toKotlinInstant
import kotlinx.datetime.Instant
import java.util.*

public actual open class Document
internal constructor(actual: com.couchbase.lite.Document) :
    DelegatedClass<com.couchbase.lite.Document>(actual), Iterable<String> {

    public actual val id: String
        get() = actual.id

    public actual val revisionID: String?
        get() = actual.revisionID

    public actual val sequence: Long
        get() = actual.sequence

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual val count: Int
        get() = actual.count()

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

    public actual open fun getArray(key: String): Array? =
        actual.getArray(key)?.asArray()

    public actual open fun getDictionary(key: String): Dictionary? =
        actual.getDictionary(key)?.asDictionary()

    public actual fun toMap(): Map<String, Any?> =
        actual.toMap().delegateIfNecessary()

    public actual fun toJSON(): String? =
        actual.toJSON()

    public actual operator fun contains(key: String): Boolean =
        actual.contains(key)

    actual override operator fun iterator(): Iterator<String> =
        actual.iterator()
}

internal fun com.couchbase.lite.Document.asDocument() = Document(this)
