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

    public actual fun getValue(key: String): Any? =
        actual.getValue(key, isMutable)

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
        actual.getBlob(key)

    public actual fun getDate(key: String): Instant? =
        actual.getDate(key)

    public actual open fun getArray(key: String): Array? =
        getValue(key) as? Array

    public actual open fun getDictionary(key: String): Dictionary? =
        getValue(key) as? Dictionary

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
