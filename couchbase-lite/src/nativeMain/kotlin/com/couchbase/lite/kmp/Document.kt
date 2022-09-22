package com.couchbase.lite.kmp

import cnames.structs.CBLDocument
import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.keys
import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner

public actual open class Document
internal constructor(internal open val actual: CPointer<CBLDocument>) : Iterable<String> {

    init {
        CBLDocument_Retain(actual)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLDocument_Release(it)
    }

    protected open val isMutable: Boolean = false

    protected open val properties: FLDict
        get() = CBLDocument_Properties(actual)!!

    public actual val id: String
        get() = CBLDocument_ID(actual).toKString()!!

    public actual val revisionID: String?
        get() = CBLDocument_RevisionID(actual).toKString()

    public actual val sequence: Long
        get() = CBLDocument_Sequence(actual).toLong()

    public actual open fun toMutable(): MutableDocument =
        MutableDocument(CBLDocument_MutableCopy(actual)!!)

    public actual val count: Int
        get() = FLDict_Count(properties).toInt()

    public actual val keys: List<String>
        get() = properties.keys()

    public actual fun getValue(key: String): Any? =
        properties.getValue(key, isMutable)

    public actual fun getString(key: String): String? =
        properties.getString(key)

    public actual fun getNumber(key: String): Number? =
        properties.getNumber(key)

    public actual fun getInt(key: String): Int =
        properties.getInt(key)

    public actual fun getLong(key: String): Long =
        properties.getLong(key)

    public actual fun getFloat(key: String): Float =
        properties.getFloat(key)

    public actual fun getDouble(key: String): Double =
        properties.getDouble(key)

    public actual fun getBoolean(key: String): Boolean =
        properties.getBoolean(key)

    public actual fun getBlob(key: String): Blob? =
        properties.getBlob(key)

    public actual fun getDate(key: String): Instant? =
        properties.getDate(key)

    public actual open fun getArray(key: String): Array? =
        getValue(key) as? Array

    public actual open fun getDictionary(key: String): Dictionary? =
        getValue(key) as? Dictionary

    public actual fun toMap(): Map<String, Any?> =
        properties.toMap()

    public actual open fun toJSON(): String? =
        FLValue_ToJSON(properties.reinterpret()).toKString()!!

    public actual operator fun contains(key: String): Boolean =
        keys.contains(key)

    actual override operator fun iterator(): Iterator<String> =
        keys.iterator()
}

internal fun CPointer<CBLDocument>.asDocument() = Document(this)
