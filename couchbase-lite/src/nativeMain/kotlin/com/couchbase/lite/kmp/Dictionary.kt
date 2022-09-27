package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner

public actual open class Dictionary
internal constructor(internal open val actual: FLDict) : Iterable<String> {

    init {
        FLDict_Retain(actual)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        FLDict_Release(it)
    }

    protected open val isMutable: Boolean = false

    public actual fun toMutable(): MutableDictionary =
        MutableDictionary(FLDict_AsMutable(actual) ?: FLDict_MutableCopy(actual, kFLDeepCopy)!!)

    public actual val count: Int
        get() = FLDict_Count(actual).toInt()

    public actual val keys: List<String>
        get() = actual.keys()

    private fun getFLValue(key: String): FLValue? =
        actual.getValue(key)

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
        actual.toMap()

    public actual open fun toJSON(): String =
        FLValue_ToJSON(actual.reinterpret()).toKString()!!

    public actual operator fun contains(key: String): Boolean =
        keys.contains(key)

    override fun iterator(): Iterator<String> =
        keys.iterator()
}

internal fun FLDict.asDictionary() = Dictionary(this)
