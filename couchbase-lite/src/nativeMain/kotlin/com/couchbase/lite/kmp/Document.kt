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

    internal open val properties: FLDict
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

    private fun getFLValue(key: String): FLValue? =
        properties.getValue(key)

    public actual fun getValue(key: String): Any? =
        getFLValue(key)?.toNative(isMutable)

    public actual fun getString(key: String): String? =
        getFLValue(key)?.toKString()

    public actual fun getNumber(key: String): Number? =
        getFLValue(key)?.toNumber()

    public actual fun getInt(key: String): Int =
        getFLValue(key).toInt()

    public actual fun getLong(key: String): Long =
        getFLValue(key).toLong()

    public actual fun getFloat(key: String): Float =
        getFLValue(key).toFloat()

    public actual fun getDouble(key: String): Double =
        getFLValue(key).toDouble()

    public actual fun getBoolean(key: String): Boolean =
        getFLValue(key).toBoolean()

    public actual fun getBlob(key: String): Blob? =
        getFLValue(key)?.toBlob()

    public actual fun getDate(key: String): Instant? =
        getFLValue(key)?.toDate()

    public actual open fun getArray(key: String): Array? =
        getFLValue(key)?.toArray(isMutable)

    public actual open fun getDictionary(key: String): Dictionary? =
        getFLValue(key)?.toDictionary(isMutable)

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
