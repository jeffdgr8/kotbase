@file:JvmName("MutableArrayJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import com.udobny.kmp.ext.toDate
import kotlinx.datetime.Instant

public actual class MutableArray
internal constructor(override val actual: com.couchbase.lite.MutableArray) : Array(actual) {

    public actual constructor() : this(com.couchbase.lite.MutableArray())

    public actual constructor(data: List<Any?>) : this(
        com.couchbase.lite.MutableArray(data.actualIfDelegated())
    )

    public actual constructor(json: String) : this(com.couchbase.lite.MutableArray(json))

    public actual fun setData(data: List<Any?>): MutableArray {
        actual.setData(data.actualIfDelegated())
        return this
    }

    public actual fun setJSON(json: String): MutableArray {
        actual.setJSON(json)
        return this
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray {
        actual.setValue(index, value?.actualIfDelegated())
        return this
    }

    public actual fun setString(index: Int, value: String?): MutableArray {
        actual.setString(index, value)
        return this
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray {
        actual.setNumber(index, value)
        return this
    }

    public actual fun setInt(index: Int, value: Int): MutableArray {
        actual.setInt(index, value)
        return this
    }

    public actual fun setLong(index: Int, value: Long): MutableArray {
        actual.setLong(index, value)
        return this
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray {
        actual.setFloat(index, value)
        return this
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray {
        actual.setDouble(index, value)
        return this
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray {
        actual.setBoolean(index, value)
        return this
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray {
        actual.setBlob(index, value?.actual)
        return this
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray {
        actual.setDate(index, value?.toDate())
        return this
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray {
        actual.setArray(index, value?.actual)
        return this
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        actual.setDictionary(index, value?.actual)
        return this
    }

    public actual fun addValue(value: Any?): MutableArray {
        actual.addValue(value?.actualIfDelegated())
        return this
    }

    public actual fun addString(value: String?): MutableArray {
        actual.addString(value)
        return this
    }

    public actual fun addNumber(value: Number?): MutableArray {
        actual.addNumber(value)
        return this
    }

    public actual fun addInt(value: Int): MutableArray {
        actual.addInt(value)
        return this
    }

    public actual fun addLong(value: Long): MutableArray {
        actual.addLong(value)
        return this
    }

    public actual fun addFloat(value: Float): MutableArray {
        actual.addFloat(value)
        return this
    }

    public actual fun addDouble(value: Double): MutableArray {
        actual.addDouble(value)
        return this
    }

    public actual fun addBoolean(value: Boolean): MutableArray {
        actual.addBoolean(value)
        return this
    }

    public actual fun addBlob(value: Blob?): MutableArray {
        actual.addBlob(value?.actual)
        return this
    }

    public actual fun addDate(value: Instant?): MutableArray {
        actual.addDate(value?.toDate())
        return this
    }

    public actual fun addArray(value: Array?): MutableArray {
        actual.addArray(value?.actual)
        return this
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray {
        actual.addDictionary(value?.actual)
        return this
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray {
        actual.insertValue(index, value?.actualIfDelegated())
        return this
    }

    public actual fun insertString(index: Int, value: String?): MutableArray {
        actual.insertString(index, value)
        return this
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray {
        actual.insertNumber(index, value)
        return this
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray {
        actual.insertInt(index, value)
        return this
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray {
        actual.insertLong(index, value)
        return this
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray {
        actual.insertFloat(index, value)
        return this
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray {
        actual.insertDouble(index, value)
        return this
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray {
        actual.insertBoolean(index, value)
        return this
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray {
        actual.insertBlob(index, value?.actual)
        return this
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray {
        actual.insertDate(index, value?.toDate())
        return this
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray {
        actual.insertArray(index, value?.actual)
        return this
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        actual.insertDictionary(index, value?.actual)
        return this
    }

    public actual fun remove(index: Int): MutableArray {
        actual.remove(index)
        return this
    }

    actual override fun getArray(index: Int): MutableArray? =
        actual.getArray(index)?.asMutableArray()

    actual override fun getDictionary(index: Int): MutableDictionary? =
        actual.getDictionary(index)?.asMutableDictionary()
}

internal fun com.couchbase.lite.MutableArray.asMutableArray() = MutableArray(this)
