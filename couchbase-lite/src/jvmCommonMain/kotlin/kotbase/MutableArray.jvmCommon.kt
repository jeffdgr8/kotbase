package kotbase

import kotbase.ext.toDate
import kotlinx.datetime.Instant
import com.couchbase.lite.MutableArray as CBLMutableArray

public actual class MutableArray
internal constructor(actual: CBLMutableArray) : Array(actual) {

    public actual constructor() : this(CBLMutableArray())

    public actual constructor(data: List<Any?>) : this(CBLMutableArray(data.actualIfDelegated()))

    public actual constructor(json: String) : this(CBLMutableArray(json))

    public actual fun setData(data: List<Any?>): MutableArray {
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        return this
    }

    public actual fun setJSON(json: String): MutableArray {
        collectionMap.clear()
        actual.setJSON(json)
        return this
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray {
        actual.setValue(index, value?.actualIfDelegated())
        if (value is Array || value is Dictionary) {
            collectionMap[index] = value
        } else {
            collectionMap.remove(index)
        }
        return this
    }

    public actual fun setString(index: Int, value: String?): MutableArray {
        actual.setString(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray {
        actual.setNumber(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setInt(index: Int, value: Int): MutableArray {
        actual.setInt(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setLong(index: Int, value: Long): MutableArray {
        actual.setLong(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray {
        actual.setFloat(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray {
        actual.setDouble(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray {
        actual.setBoolean(index, value)
        collectionMap.remove(index)
        return this
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray {
        actual.setBlob(index, value?.actual)
        collectionMap.remove(index)
        return this
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray {
        actual.setDate(index, value?.toDate())
        collectionMap.remove(index)
        return this
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray {
        actual.setArray(index, value?.actual)
        if (value == null) {
            collectionMap.remove(index)
        } else {
            collectionMap[index] = value
        }
        return this
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        actual.setDictionary(index, value?.actual)
        if (value == null) {
            collectionMap.remove(index)
        } else {
            collectionMap[index] = value
        }
        return this
    }

    public actual fun addValue(value: Any?): MutableArray {
        actual.addValue(value?.actualIfDelegated())
        if (value is Array || value is Dictionary) {
            collectionMap[count - 1] = value
        }
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
        if (value != null) {
            collectionMap[count - 1] = value
        }
        return this
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray {
        actual.addDictionary(value?.actual)
        if (value != null) {
            collectionMap[count - 1] = value
        }
        return this
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray {
        actual.insertValue(index, value?.actualIfDelegated())
        incrementAfter(index, collectionMap)
        if (value is Array || value is Dictionary) {
            collectionMap[index] = value
        }
        return this
    }

    public actual fun insertString(index: Int, value: String?): MutableArray {
        actual.insertString(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray {
        actual.insertNumber(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray {
        actual.insertInt(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray {
        actual.insertLong(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray {
        actual.insertFloat(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray {
        actual.insertDouble(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray {
        actual.insertBoolean(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray {
        actual.insertBlob(index, value?.actual)
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray {
        actual.insertDate(index, value?.toDate())
        incrementAfter(index, collectionMap)
        return this
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray {
        actual.insertArray(index, value?.actual)
        incrementAfter(index, collectionMap)
        if (value != null) {
            collectionMap[index] = value
        }
        return this
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        actual.insertDictionary(index, value?.actual)
        incrementAfter(index, collectionMap)
        if (value != null) {
            collectionMap[index] = value
        }
        return this
    }

    public actual fun remove(index: Int): MutableArray {
        actual.remove(index)
        collectionMap.remove(index)
        return this
    }

    actual override fun getArray(index: Int): MutableArray? {
        return getInternalCollection(index)
            ?: actual.getArray(index)?.asMutableArray()
                ?.also { collectionMap[index] = it }
    }

    actual override fun getDictionary(index: Int): MutableDictionary? {
        return getInternalCollection(index)
            ?: actual.getDictionary(index)?.asMutableDictionary()
                ?.also { collectionMap[index] = it }
    }
}

internal val MutableArray.actual: CBLMutableArray
    get() = platformState.actual as CBLMutableArray

internal fun CBLMutableArray.asMutableArray() = MutableArray(this)
