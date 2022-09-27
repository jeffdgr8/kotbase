package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.toKString
import com.couchbase.lite.kmp.internal.fleece.toList
import com.couchbase.lite.kmp.internal.fleece.toNative
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.datetime.Instant
import libcblite.*
import kotlin.native.internal.createCleaner

public actual class Result
private constructor(
    private val query: CPointer<CBLQuery>?,
    private val array: FLArray?,
    private val dict: FLDict?
) : Iterable<String> {

    internal constructor(rs: CPointer<CBLResultSet>) : this(
        CBLResultSet_GetQuery(rs),
        CBLResultSet_ResultArray(rs),
        CBLResultSet_ResultDict(rs)
    )

    private val memory = object {
        val query = this@Result.query
        val array = this@Result.array
        val dict = this@Result.dict
    }

    init {
        CBLQuery_Retain(query)
        FLArray_Retain(array)
        FLDict_Retain(dict)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(memory) {
        CBLQuery_Release(it.query)
        FLArray_Release(it.array)
        FLDict_Release(it.dict)
    }

    public actual val count: Int
        get() = CBLQuery_ColumnCount(query).toInt()

    private fun getFLValue(index: Int): FLValue? {
        assertInBounds(index)
        return FLArray_Get(array, index.convert())
    }

    public actual fun getValue(index: Int): Any? =
        getFLValue(index)?.toNative(false)

    public actual fun getString(index: Int): String? =
        getFLValue(index)?.toKString()

    public actual fun getNumber(index: Int): Number? =
        getFLValue(index)?.toNumber()

    public actual fun getInt(index: Int): Int =
        getFLValue(index).toInt()

    public actual fun getLong(index: Int): Long =
        getFLValue(index).toLong()

    public actual fun getFloat(index: Int): Float =
        getFLValue(index).toFloat()

    public actual fun getDouble(index: Int): Double =
        getFLValue(index).toDouble()

    public actual fun getBoolean(index: Int): Boolean =
        getFLValue(index).toBoolean()

    public actual fun getBlob(index: Int): Blob? =
        getFLValue(index)?.toBlob()

    public actual fun getDate(index: Int): Instant? =
        getFLValue(index)?.toDate()

    public actual fun getArray(index: Int): Array? =
        getFLValue(index)?.toArray(false)

    public actual fun getDictionary(index: Int): Dictionary? =
        getFLValue(index)?.toDictionary(false)

    public actual fun toList(): List<Any?> =
        array?.toList() ?: emptyList()

    public actual val keys: List<String>
        get() = buildList {
            repeat(count) { index ->
                add(CBLQuery_ColumnName(query, index.convert()).toKString()!!)
            }
        }

    private fun getFLValue(key: String): FLValue? {
        return memScoped {
            FLDict_Get(dict, key.toFLString(this))
        }
    }

    public actual fun getValue(key: String): Any? =
        getFLValue(key)?.toNative(false)

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

    public actual fun getArray(key: String): Array? =
        getFLValue(key)?.toArray(false)

    public actual fun getDictionary(key: String): Dictionary? =
        getFLValue(key)?.toDictionary(false)

    public actual fun toMap(): Map<String, Any?> =
        dict?.toMap() ?: emptyMap()

    public actual fun toJSON(): String =
        FLValue_ToJSON(dict?.reinterpret()).toKString()!!

    public actual operator fun contains(key: String): Boolean =
        keys.contains(key)

    actual override fun iterator(): Iterator<String> =
        keys.iterator()

    private fun isInBounds(index: Int): Boolean {
        return index in 0 until count
    }

    private fun assertInBounds(index: Int) {
        if (!isInBounds(index)) {
            throw ArrayIndexOutOfBoundsException("index $index must be between 0 and $count")
        }
    }
}
