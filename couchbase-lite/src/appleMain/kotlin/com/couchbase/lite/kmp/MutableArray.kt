package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMutableArray
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.chain
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class MutableArray
internal constructor(override val actual: CBLMutableArray) : Array(actual) {

    public actual constructor() : this(CBLMutableArray())

    public actual constructor(data: List<Any?>) : this(
        CBLMutableArray(data.actualIfDelegated())
    ) {
        setBooleans(data)
    }

    public actual constructor(json: String) : this() {
        setJSON(json)
    }

    private inline fun chain(action: CBLMutableArray.() -> Unit) = chain(actual, action)

    private fun setBooleans(data: List<Any?>) {
        data.forEachIndexed { index, value ->
            if (value is Boolean) {
                // Booleans treated as numbers unless explicitly using boolean API
                setBoolean(index, value)
            }
        }
    }

    public actual fun setData(data: List<Any?>): MutableArray = chain {
        data.forEach { checkSelf(it) }
        setData(data.actualIfDelegated())
        setBooleans(data)
    }

    public actual fun setJSON(json: String): MutableArray = chain {
        try {
            wrapCBLError { error ->
                actual.setJSON(json, error)
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray = chain {
        checkSelf(value)
        checkType(value)
        checkIndex(index)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> setBoolean(value, index.convert())
            else -> setValue(value?.actualIfDelegated(), index.convert())
        }
    }

    public actual fun setString(index: Int, value: String?): MutableArray = chain {
        checkIndex(index)
        setString(value, index.convert())
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray = chain {
        checkIndex(index)
        setNumber(value as NSNumber?, index.convert())
    }

    public actual fun setInt(index: Int, value: Int): MutableArray = chain {
        checkIndex(index)
        setInteger(value.convert(), index.convert())
    }

    public actual fun setLong(index: Int, value: Long): MutableArray = chain {
        checkIndex(index)
        setLongLong(value, index.convert())
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray = chain {
        checkIndex(index)
        setFloat(value, index.convert())
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray = chain {
        checkIndex(index)
        setDouble(value, index.convert())
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray = chain {
        checkIndex(index)
        setBoolean(value, index.convert())
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray = chain {
        checkIndex(index)
        setBlob(value?.actual, index.convert())
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray = chain {
        checkIndex(index)
        setDate(value?.toNSDate(), index.convert())
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray = chain {
        checkSelf(value)
        checkIndex(index)
        setArray(value?.actual, index.convert())
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray = chain {
        checkIndex(index)
        setDictionary(value?.actual, index.convert())
    }

    public actual fun addValue(value: Any?): MutableArray = chain {
        checkSelf(value)
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> addBoolean(value)
            else -> addValue(value?.actualIfDelegated())
        }
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
        checkType(value)
        checkInsertIndex(index)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> insertBoolean(value, index.convert())
            else -> insertValue(value?.actualIfDelegated(), index.convert())
        }
    }

    public actual fun insertString(index: Int, value: String?): MutableArray = chain {
        checkInsertIndex(index)
        insertString(value, index.convert())
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray = chain {
        checkInsertIndex(index)
        insertNumber(value as NSNumber?, index.convert())
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray = chain {
        checkInsertIndex(index)
        insertInteger(value.convert(), index.convert())
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray = chain {
        checkInsertIndex(index)
        insertLongLong(value, index.convert())
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray = chain {
        checkInsertIndex(index)
        insertFloat(value, index.convert())
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray = chain {
        checkInsertIndex(index)
        insertDouble(value, index.convert())
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray = chain {
        checkInsertIndex(index)
        insertBoolean(value, index.convert())
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray = chain {
        checkInsertIndex(index)
        insertBlob(value?.actual, index.convert())
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray = chain {
        checkInsertIndex(index)
        insertDate(value?.toNSDate(), index.convert())
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray = chain {
        checkSelf(value)
        checkInsertIndex(index)
        insertArray(value?.actual, index.convert())
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray = chain {
        checkInsertIndex(index)
        insertDictionary(value?.actual, index.convert())
    }

    public actual fun remove(index: Int): MutableArray = chain {
        checkIndex(index)
        removeValueAtIndex(index.convert())
    }

    actual override fun getArray(index: Int): MutableArray? {
        checkIndex(index)
        return actual.arrayAtIndex(index.convert())?.asMutableArray()
    }

    actual override fun getDictionary(index: Int): MutableDictionary? {
        checkIndex(index)
        return actual.dictionaryAtIndex(index.convert())?.asMutableDictionary()
    }

    override fun toJSON(): String {
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }

    // Java performs this check, but Objective-C does not
    private fun checkSelf(value: Any?) {
        if (value === this) {
            throw IllegalArgumentException("Arrays cannot ba added to themselves")
        }
    }

    // Throw IndexOutOfBoundException, avoid Objective-C NSRangeException
    private fun checkInsertIndex(index: Int) {
        if (index < 0 || index > count) {
            throw IndexOutOfBoundsException("Array index $index is out of range")
        }
    }
}

internal fun CBLMutableArray.asMutableArray() = MutableArray(this)
