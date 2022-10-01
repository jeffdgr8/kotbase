package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.parseJson
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.fleece.wrapFLError
import com.udobny.kmp.ext.toStringMillis
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import libcblite.*

public actual class MutableArray
internal constructor(override val actual: FLMutableArray) : Array(actual) {

    public actual constructor() : this(FLMutableArray_New()!!) {
        // TODO: make sure this is called after it's retained for the second time
        FLMutableArray_Release(actual)
    }

    public actual constructor(data: List<Any?>) : this() {
        setData(data)
    }

    public actual constructor(json: String) : this(
        // TODO: fix double retain
        wrapFLError { error ->
            FLMutableArray_NewFromJSON(json.toFLString(), error)!!
        }
    )

    public actual fun setData(data: List<Any?>): MutableArray {
        FLMutableArray_Resize(actual, data.size.convert())
        data.forEachIndexed { index, value ->
            setValue(index, value)
        }
        return this
    }

    public actual fun setJSON(json: String): MutableArray {
        val data = parseJson(json) as? List<Any?>
            ?: error("Parsed result is not an Array")
        setData(data)
        return this
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray {
        checkIndex(index)
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Boolean -> setBoolean(index, value)
            is ByteArray -> setBlob(index, Blob(value))
            is Blob -> setBlob(index, value)
            is String -> setString(index, value)
            is Instant -> setDate(index, value)
            is Number -> setNumber(index, value)
            is List<*> -> setArray(index, MutableArray(value))
            is Array -> setArray(index, value)
            is Map<*, *> -> setDictionary(index, MutableDictionary(value as Map<String, Any?>))
            is Dictionary -> setDictionary(index, value)
            null -> FLMutableArray_SetNull(actual, index.convert())
            else -> invalidTypeError(value)
        }
        return this
    }

    public actual fun setString(index: Int, value: String?): MutableArray {
        checkIndex(index)
        if (value != null) {
            FLMutableArray_SetString(actual, index.convert(), value.toFLString())
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray {
        checkIndex(index)
        when (value) {
            is Double -> FLMutableArray_SetDouble(actual, index.convert(), value)
            is Float -> FLMutableArray_SetFloat(actual, index.convert(), value)
            null -> FLMutableArray_SetNull(actual, index.convert())
            else -> FLMutableArray_SetInt(actual, index.convert(), value.toLong().convert())
        }
        return this
    }

    public actual fun setInt(index: Int, value: Int): MutableArray {
        checkIndex(index)
        FLMutableArray_SetInt(actual, index.convert(), value.convert())
        return this
    }

    public actual fun setLong(index: Int, value: Long): MutableArray {
        checkIndex(index)
        FLMutableArray_SetInt(actual, index.convert(), value.convert())
        return this
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray {
        checkIndex(index)
        FLMutableArray_SetFloat(actual, index.convert(), value)
        return this
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray {
        checkIndex(index)
        FLMutableArray_SetDouble(actual, index.convert(), value)
        return this
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray {
        checkIndex(index)
        FLMutableArray_SetBool(actual, index.convert(), value)
        return this
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray {
        checkIndex(index)
        if (value != null) {
            FLMutableArray_SetBlob(actual, index.convert(), value.actual)
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray {
        checkIndex(index)
        if (value != null) {
            FLMutableArray_SetString(actual, index.convert(), value.toStringMillis().toFLString())
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray {
        checkIndex(index)
        if (value != null) {
            checkSelf(value.actual)
            FLMutableArray_SetArray(actual, index.convert(), value.actual)
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        checkIndex(index)
        if (value != null) {
            FLMutableArray_SetDict(actual, index.convert(), value.actual)
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun addValue(value: Any?): MutableArray {
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Boolean -> addBoolean(value)
            is ByteArray -> addBlob(Blob(value))
            is Blob -> addBlob(value)
            is String -> addString(value)
            is Instant -> addDate(value)
            is Number -> addNumber(value)
            is List<*> -> addArray(MutableArray(value))
            is Array -> addArray(value)
            is Map<*, *> -> addDictionary(MutableDictionary(value as Map<String, Any?>))
            is Dictionary -> addDictionary(value)
            null -> FLMutableArray_AppendNull(actual)
            else -> invalidTypeError(value)
        }
        return this
    }

    public actual fun addString(value: String?): MutableArray {
        if (value != null) {
            FLMutableArray_AppendString(actual, value.toFLString())
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addNumber(value: Number?): MutableArray {
        if (value != null) {
            when (value) {
                is Double -> FLMutableArray_AppendDouble(actual, value)
                is Float -> FLMutableArray_AppendFloat(actual, value)
                null -> FLMutableArray_AppendNull(actual)
                else -> FLMutableArray_AppendInt(actual, value.toLong().convert())
            }
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addInt(value: Int): MutableArray {
        FLMutableArray_AppendInt(actual, value.convert())
        return this
    }

    public actual fun addLong(value: Long): MutableArray {
        FLMutableArray_AppendInt(actual, value.convert())
        return this
    }

    public actual fun addFloat(value: Float): MutableArray {
        FLMutableArray_AppendFloat(actual, value)
        return this
    }

    public actual fun addDouble(value: Double): MutableArray {
        FLMutableArray_AppendDouble(actual, value)
        return this
    }

    public actual fun addBoolean(value: Boolean): MutableArray {
        FLMutableArray_AppendBool(actual, value)
        return this
    }

    public actual fun addBlob(value: Blob?): MutableArray {
        if (value != null) {
            FLMutableArray_AppendBlob(actual, value.actual)
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addDate(value: Instant?): MutableArray {
        if (value != null) {
            FLMutableArray_AppendString(actual, value.toStringMillis().toFLString())
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addArray(value: Array?): MutableArray {
        if (value != null) {
            checkSelf(value.actual)
            FLMutableArray_AppendArray(actual, value.actual)
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray {
        if (value != null) {
            FLMutableArray_AppendDict(actual, value.actual)
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setValue(index, value)
        return this
    }

    public actual fun insertString(index: Int, value: String?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setString(index, value)
        return this
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setNumber(index, value)
        return this
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setInt(index, value)
        return this
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setLong(index, value)
        return this
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setFloat(index, value)
        return this
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setDouble(index, value)
        return this
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setBoolean(index, value)
        return this
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setBlob(index, value)
        return this
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setDate(index, value)
        return this
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setArray(index, value)
        return this
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1)
        setDictionary(index, value)
        return this
    }

    public actual fun remove(index: Int): MutableArray {
        checkIndex(index)
        FLMutableArray_Remove(actual, index.convert(), 1)
        return this
    }

    override fun getValue(index: Int): Any? =
        getFLValue(index)?.toMutableNative { setValue(index, it) }

    actual override fun getArray(index: Int): MutableArray? =
        getFLValue(index)?.toMutableArray { setArray(index, it) }

    actual override fun getDictionary(index: Int): MutableDictionary? =
        getFLValue(index)?.toMutableDictionary { setDictionary(index, it) }

    private fun checkSelf(value: FLMutableArray) {
        if (value === actual) {
            throw IllegalArgumentException("Arrays cannot ba added to themselves")
        }
    }

    private fun checkInsertIndex(index: Int) {
        if (index < 0 || index > count) {
            throw IndexOutOfBoundsException("Array index $index is out of range")
        }
    }
}

internal fun FLMutableArray.asMutableArray() = MutableArray(this)
