package kotbase

import cocoapods.CouchbaseLite.CBLMutableArray
import kotbase.ext.wrapCBLError
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

    private fun setBooleans(data: List<Any?>) {
        data.forEachIndexed { index, value ->
            if (value is Boolean) {
                // Booleans treated as numbers unless explicitly using boolean API
                setBoolean(index, value)
            }
        }
    }

    public actual fun setData(data: List<Any?>): MutableArray {
        data.forEach { checkSelf(it) }
        actual.setData(data.actualIfDelegated())
        setBooleans(data)
        return this
    }

    public actual fun setJSON(json: String): MutableArray {
        try {
            wrapCBLError { error ->
                actual.setJSON(json, error)
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
        return this
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray {
        checkSelf(value)
        checkType(value)
        checkIndex(index)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.setBoolean(value, index.convert())
            else -> actual.setValue(value?.actualIfDelegated(), index.convert())
        }
        return this
    }

    public actual fun setString(index: Int, value: String?): MutableArray {
        checkIndex(index)
        actual.setString(value, index.convert())
        return this
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray {
        checkIndex(index)
        actual.setNumber(value as NSNumber?, index.convert())
        return this
    }

    public actual fun setInt(index: Int, value: Int): MutableArray {
        checkIndex(index)
        actual.setInteger(value.convert(), index.convert())
        return this
    }

    public actual fun setLong(index: Int, value: Long): MutableArray {
        checkIndex(index)
        actual.setLongLong(value, index.convert())
        return this
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray {
        checkIndex(index)
        actual.setFloat(value, index.convert())
        return this
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray {
        checkIndex(index)
        actual.setDouble(value, index.convert())
        return this
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray {
        checkIndex(index)
        actual.setBoolean(value, index.convert())
        return this
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray {
        checkIndex(index)
        actual.setBlob(value?.actual, index.convert())
        return this
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray {
        checkIndex(index)
        actual.setDate(value?.toNSDate(), index.convert())
        return this
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray {
        checkSelf(value)
        checkIndex(index)
        actual.setArray(value?.actual, index.convert())
        return this
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        checkIndex(index)
        actual.setDictionary(value?.actual, index.convert())
        return this
    }

    public actual fun addValue(value: Any?): MutableArray {
        checkSelf(value)
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.addBoolean(value)
            else -> actual.addValue(value?.actualIfDelegated())
        }
        return this
    }

    public actual fun addString(value: String?): MutableArray {
        actual.addString(value)
        return this
    }

    public actual fun addNumber(value: Number?): MutableArray {
        actual.addNumber(value as NSNumber?)
        return this
    }

    public actual fun addInt(value: Int): MutableArray {
        actual.addInteger(value.convert())
        return this
    }

    public actual fun addLong(value: Long): MutableArray {
        actual.addLongLong(value)
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
        actual.addDate(value?.toNSDate())
        return this
    }

    public actual fun addArray(value: Array?): MutableArray {
        checkSelf(value)
        actual.addArray(value?.actual)
        return this
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray {
        actual.addDictionary(value?.actual)
        return this
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray {
        checkSelf(value)
        checkType(value)
        checkInsertIndex(index)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.insertBoolean(value, index.convert())
            else -> actual.insertValue(value?.actualIfDelegated(), index.convert())
        }
        return this
    }

    public actual fun insertString(index: Int, value: String?): MutableArray {
        checkInsertIndex(index)
        actual.insertString(value, index.convert())
        return this
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray {
        checkInsertIndex(index)
        actual.insertNumber(value as NSNumber?, index.convert())
        return this
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray {
        checkInsertIndex(index)
        actual.insertInteger(value.convert(), index.convert())
        return this
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray {
        checkInsertIndex(index)
        actual.insertLongLong(value, index.convert())
        return this
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray {
        checkInsertIndex(index)
        actual.insertFloat(value, index.convert())
        return this
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray {
        checkInsertIndex(index)
        actual.insertDouble(value, index.convert())
        return this
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray {
        checkInsertIndex(index)
        actual.insertBoolean(value, index.convert())
        return this
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray {
        checkInsertIndex(index)
        actual.insertBlob(value?.actual, index.convert())
        return this
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray {
        checkInsertIndex(index)
        actual.insertDate(value?.toNSDate(), index.convert())
        return this
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray {
        checkSelf(value)
        checkInsertIndex(index)
        actual.insertArray(value?.actual, index.convert())
        return this
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        checkInsertIndex(index)
        actual.insertDictionary(value?.actual, index.convert())
        return this
    }

    public actual fun remove(index: Int): MutableArray {
        checkIndex(index)
        actual.removeValueAtIndex(index.convert())
        return this
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