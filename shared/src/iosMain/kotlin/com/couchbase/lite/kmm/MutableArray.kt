package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLMutableArray
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.chain
import com.udobny.kmm.ext.*
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSError
import platform.Foundation.NSNumber

public actual class MutableArray
internal constructor(override val actual: CBLMutableArray) : Array(actual) {

    public actual constructor() : this(CBLMutableArray())

    public actual constructor(data: List<Any?>) : this(
        CBLMutableArray(data.actualIfDelegated())
    )

    public actual constructor(json: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            CBLMutableArray(json, error)
        }
    )

    private inline fun chain(action: CBLMutableArray.() -> Unit) = chain(actual, action)

    public actual fun setData(data: List<Any?>): MutableArray = chain {
        data.forEach { checkSelf(it) }
        setData(data.actualIfDelegated())
    }

    public actual fun setJSON(json: String): MutableArray = chain {
        throwError { error ->
            setJSON(json, error)
        }
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray = chain {
        checkSelf(value)
        setValue(value?.actualIfDelegated(), index.convert())
    }

    public actual fun setString(index: Int, value: String?): MutableArray = chain {
        setString(value, index.convert())
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray = chain {
        setNumber(value as NSNumber?, index.convert())
    }

    public actual fun setInt(index: Int, value: Int): MutableArray = chain {
        setInteger(value.convert(), index.convert())
    }

    public actual fun setLong(index: Int, value: Long): MutableArray = chain {
        setLongLong(value, index.convert())
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray = chain {
        setFloat(value, index.convert())
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray = chain {
        setDouble(value, index.convert())
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray = chain {
        setBoolean(value, index.convert())
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray = chain {
        setBlob(value?.actual, index.convert())
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray = chain {
        checkSelf(value)
        setArray(value?.actual, index.convert())
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray = chain {
        setDate(value?.toNSDate(), index.convert())
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray = chain {
        setDictionary(value?.actual, index.convert())
    }

    public actual fun addValue(value: Any?): MutableArray = chain {
        checkSelf(value)
        addValue(value?.actualIfDelegated())
    }

    public actual fun addString(value: String?): MutableArray = chain {
        addString(value)
    }

    public actual fun addNumber(value: Number?): MutableArray = chain {
        addNumber(value as NSNumber?)
    }

    public actual fun addInt(value: Int): MutableArray = chain {
        addInteger(value.convert())
    }

    public actual fun addLong(value: Long): MutableArray = chain {
        addLongLong(value)
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
        addDate(value?.toNSDate())
    }

    public actual fun addArray(value: Array?): MutableArray = chain {
        checkSelf(value)
        addArray(value?.actual)
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray = chain {
        addDictionary(value?.actual)
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray = chain {
        checkSelf(value)
        insertValue(value?.actualIfDelegated(), index.convert())
    }

    public actual fun insertString(index: Int, value: String?): MutableArray = chain {
        insertString(value, index.convert())
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray = chain {
        insertNumber(value as NSNumber?, index.convert())
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray = chain {
        insertInteger(value.convert(), index.convert())
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray = chain {
        insertLongLong(value, index.convert())
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray = chain {
        insertFloat(value, index.convert())
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray = chain {
        insertDouble(value, index.convert())
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray = chain {
        insertBoolean(value, index.convert())
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray = chain {
        insertBlob(value?.actual, index.convert())
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray = chain {
        insertDate(value?.toNSDate(), index.convert())
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray = chain {
        checkSelf(value)
        insertArray(value?.actual, index.convert())
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray = chain {
        insertDictionary(value?.actual, index.convert())
    }

    public actual fun remove(index: Int): MutableArray = chain {
        removeValueAtIndex(index.convert())
    }

    actual override fun getArray(index: Int): MutableArray? =
        actual.arrayAtIndex(index.convert())?.asMutableArray()

    actual override fun getDictionary(index: Int): MutableDictionary? =
        actual.dictionaryAtIndex(index.convert())?.asMutableDictionary()

    // Java performs this check, but Objective-C does not
    private fun checkSelf(value: Any?) {
        if (value === this) {
            throw IllegalArgumentException("Arrays cannot ba added to themselves")
        }
    }
}

internal fun CBLMutableArray.asMutableArray() = MutableArray(this)
