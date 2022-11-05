@file:JvmName("MutableArrayJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import com.udobny.kmp.chain
import com.udobny.kmp.ext.toDate
import kotlinx.datetime.Instant

public actual class MutableArray
internal constructor(override val actual: com.couchbase.lite.MutableArray) : Array(actual) {

    public actual constructor() : this(com.couchbase.lite.MutableArray())

    public actual constructor(data: List<Any?>) : this(
        com.couchbase.lite.MutableArray(data.actualIfDelegated())
    )

    public actual constructor(json: String) : this(com.couchbase.lite.MutableArray(json))

    private inline fun chain(action: com.couchbase.lite.MutableArray.() -> Unit) =
        chain(actual, action)

    public actual fun setData(data: List<Any?>): MutableArray = chain {
        setData(data.actualIfDelegated())
    }

    public actual fun setJSON(json: String): MutableArray = chain {
        setJSON(json)
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray = chain {
        setValue(index, value?.actualIfDelegated())
    }

    public actual fun setString(index: Int, value: String?): MutableArray = chain {
        setString(index, value)
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray = chain {
        setNumber(index, value)
    }

    public actual fun setInt(index: Int, value: Int): MutableArray = chain {
        setInt(index, value)
    }

    public actual fun setLong(index: Int, value: Long): MutableArray = chain {
        setLong(index, value)
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray = chain {
        setFloat(index, value)
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray = chain {
        setDouble(index, value)
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray = chain {
        setBoolean(index, value)
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray = chain {
        setBlob(index, value?.actual)
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray = chain {
        setDate(index, value?.toDate())
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray = chain {
        setArray(index, value?.actual)
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray = chain {
        setDictionary(index, value?.actual)
    }

    public actual fun addValue(value: Any?): MutableArray = chain {
        addValue(value?.actualIfDelegated())
    }

    public actual fun addString(value: String?): MutableArray = chain {
        addString(value)
    }

    public actual fun addNumber(value: Number?): MutableArray = chain {
        addNumber(value)
    }

    public actual fun addInt(value: Int): MutableArray = chain {
        addInt(value)
    }

    public actual fun addLong(value: Long): MutableArray = chain {
        addLong(value)
    }

    public actual fun addFloat(value: Float): MutableArray = chain {
        addFloat(value)
    }

    public actual fun addDouble(value: Double): MutableArray = chain {
        addDouble(value)
    }

    public actual fun addBoolean(value: Boolean): MutableArray = chain {
        addBoolean(value)
    }

    public actual fun addBlob(value: Blob?): MutableArray = chain {
        addBlob(value?.actual)
    }

    public actual fun addDate(value: Instant?): MutableArray = chain {
        addDate(value?.toDate())
    }

    public actual fun addArray(value: Array?): MutableArray = chain {
        addArray(value?.actual)
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray = chain {
        addDictionary(value?.actual)
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray = chain {
        insertValue(index, value?.actualIfDelegated())
    }

    public actual fun insertString(index: Int, value: String?): MutableArray = chain {
        insertString(index, value)
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray = chain {
        insertNumber(index, value)
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray = chain {
        insertInt(index, value)
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray = chain {
        insertLong(index, value)
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray = chain {
        insertFloat(index, value)
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray = chain {
        insertDouble(index, value)
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray = chain {
        insertBoolean(index, value)
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray = chain {
        insertBlob(index, value?.actual)
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray = chain {
        insertDate(index, value?.toDate())
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray = chain {
        insertArray(index, value?.actual)
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray = chain {
        insertDictionary(index, value?.actual)
    }

    public actual fun remove(index: Int): MutableArray = chain {
        remove(index)
    }

    actual override fun getArray(index: Int): MutableArray? =
        actual.getArray(index)?.asMutableArray()

    actual override fun getDictionary(index: Int): MutableDictionary? =
        actual.getDictionary(index)?.asMutableDictionary()
}

internal fun com.couchbase.lite.MutableArray.asMutableArray() = MutableArray(this)
