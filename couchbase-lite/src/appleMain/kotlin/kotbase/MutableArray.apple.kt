/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import cocoapods.CouchbaseLite.CBLMutableArray
import kotbase.ext.wrapCBLError
import kotlinx.cinterop.convert
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber
import kotlin.time.Instant

public actual class MutableArray
internal constructor(override val actual: CBLMutableArray) : Array(actual), MutableArrayInterface {

    public actual constructor() : this(CBLMutableArray())

    public actual constructor(data: List<Any?>) : this(
        CBLMutableArray(data.removingBooleanValues().actualIfDelegated())
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

    actual override fun setData(data: List<Any?>): MutableArray {
        collectionMap.clear()
        actual.setData(data.removingBooleanValues().actualIfDelegated())
        setBooleans(data)
        mutate()
        return this
    }

    actual override fun setJSON(json: String): MutableArray {
        collectionMap.clear()
        try {
            wrapCBLError { error ->
                actual.setJSON(json, error)
            }
        } catch (e: CouchbaseLiteException) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
        mutate()
        return this
    }

    actual override fun setValue(index: Int, value: Any?): MutableArray {
        checkType(value)
        checkIndex(index)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.setBoolean(value, index.convert())
            else -> actual.setValue(value?.actualIfDelegated(), index.convert())
        }
        mutate()
        if (value is Array && value !== this || value is Dictionary) {
            collectionMap[index] = value
        } else {
            collectionMap.remove(index)
        }
        return this
    }

    actual override fun setString(index: Int, value: String?): MutableArray {
        checkIndex(index)
        actual.setString(value, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setNumber(index: Int, value: Number?): MutableArray {
        checkIndex(index)
        actual.setNumber(value as NSNumber?, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setInt(index: Int, value: Int): MutableArray {
        checkIndex(index)
        actual.setInteger(value.convert(), index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setLong(index: Int, value: Long): MutableArray {
        checkIndex(index)
        actual.setLongLong(value, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setFloat(index: Int, value: Float): MutableArray {
        checkIndex(index)
        actual.setFloat(value, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setDouble(index: Int, value: Double): MutableArray {
        checkIndex(index)
        actual.setDouble(value, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setBoolean(index: Int, value: Boolean): MutableArray {
        checkIndex(index)
        actual.setBoolean(value, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setBlob(index: Int, value: Blob?): MutableArray {
        checkIndex(index)
        actual.setBlob(value?.actual, index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setDate(index: Int, value: Instant?): MutableArray {
        checkIndex(index)
        actual.setDate(value?.toNSDate(), index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun setArray(index: Int, value: Array?): MutableArray {
        checkIndex(index)
        actual.setArray(value?.actual, index.convert())
        if (value != null && value !== this) {
            collectionMap[index] = value
        } else {
            collectionMap.remove(index)
        }
        mutate()
        return this
    }

    actual override fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        checkIndex(index)
        actual.setDictionary(value?.actual, index.convert())
        if (value == null) {
            collectionMap.remove(index)
        } else {
            collectionMap[index] = value
        }
        mutate()
        return this
    }

    actual override fun addValue(value: Any?): MutableArray {
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.addBoolean(value)
            else -> actual.addValue(value?.actualIfDelegated())
        }
        mutate()
        if (value is Array && value !== this || value is Dictionary) {
            collectionMap[count - 1] = value
        }
        return this
    }

    actual override fun addString(value: String?): MutableArray {
        actual.addString(value)
        mutate()
        return this
    }

    actual override fun addNumber(value: Number?): MutableArray {
        actual.addNumber(value as NSNumber?)
        mutate()
        return this
    }

    actual override fun addInt(value: Int): MutableArray {
        actual.addInteger(value.convert())
        mutate()
        return this
    }

    actual override fun addLong(value: Long): MutableArray {
        actual.addLongLong(value)
        mutate()
        return this
    }

    actual override fun addFloat(value: Float): MutableArray {
        actual.addFloat(value)
        mutate()
        return this
    }

    actual override fun addDouble(value: Double): MutableArray {
        actual.addDouble(value)
        mutate()
        return this
    }

    actual override fun addBoolean(value: Boolean): MutableArray {
        actual.addBoolean(value)
        mutate()
        return this
    }

    actual override fun addBlob(value: Blob?): MutableArray {
        actual.addBlob(value?.actual)
        mutate()
        return this
    }

    actual override fun addDate(value: Instant?): MutableArray {
        actual.addDate(value?.toNSDate())
        mutate()
        return this
    }

    actual override fun addArray(value: Array?): MutableArray {
        actual.addArray(value?.actual)
        if (value != null && value !== this) {
            collectionMap[count - 1] = value
        }
        mutate()
        return this
    }

    actual override fun addDictionary(value: Dictionary?): MutableArray {
        actual.addDictionary(value?.actual)
        if (value != null) {
            collectionMap[count - 1] = value
        }
        mutate()
        return this
    }

    actual override fun insertValue(index: Int, value: Any?): MutableArray {
        checkType(value)
        checkInsertIndex(index)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.insertBoolean(value, index.convert())
            else -> actual.insertValue(value?.actualIfDelegated(), index.convert())
        }
        incrementAfter(index, collectionMap)
        if (value is Array && value !== this || value is Dictionary) {
            collectionMap[index] = value
        }
        mutate()
        return this
    }

    actual override fun insertString(index: Int, value: String?): MutableArray {
        checkInsertIndex(index)
        actual.insertString(value, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertNumber(index: Int, value: Number?): MutableArray {
        checkInsertIndex(index)
        actual.insertNumber(value as NSNumber?, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertInt(index: Int, value: Int): MutableArray {
        checkInsertIndex(index)
        actual.insertInteger(value.convert(), index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertLong(index: Int, value: Long): MutableArray {
        checkInsertIndex(index)
        actual.insertLongLong(value, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertFloat(index: Int, value: Float): MutableArray {
        checkInsertIndex(index)
        actual.insertFloat(value, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertDouble(index: Int, value: Double): MutableArray {
        checkInsertIndex(index)
        actual.insertDouble(value, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertBoolean(index: Int, value: Boolean): MutableArray {
        checkInsertIndex(index)
        actual.insertBoolean(value, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertBlob(index: Int, value: Blob?): MutableArray {
        checkInsertIndex(index)
        actual.insertBlob(value?.actual, index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertDate(index: Int, value: Instant?): MutableArray {
        checkInsertIndex(index)
        actual.insertDate(value?.toNSDate(), index.convert())
        incrementAfter(index, collectionMap)
        mutate()
        return this
    }

    actual override fun insertArray(index: Int, value: Array?): MutableArray {
        checkInsertIndex(index)
        actual.insertArray(value?.actual, index.convert())
        incrementAfter(index, collectionMap)
        if (value != null && value !== this) {
            collectionMap[index] = value
        }
        mutate()
        return this
    }

    actual override fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        checkInsertIndex(index)
        actual.insertDictionary(value?.actual, index.convert())
        incrementAfter(index, collectionMap)
        if (value != null) {
            collectionMap[index] = value
        }
        mutate()
        return this
    }

    actual override fun remove(index: Int): MutableArray {
        checkIndex(index)
        actual.removeValueAtIndex(index.convert())
        collectionMap.remove(index)
        mutate()
        return this
    }

    actual override fun getArray(index: Int): MutableArray? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.arrayAtIndex(index.convert())?.asMutableArray()
                ?.also { collectionMap[index] = it }
    }

    actual override fun getDictionary(index: Int): MutableDictionary? {
        checkIndex(index)
        return getInternalCollection(index)
            ?: actual.dictionaryAtIndex(index.convert())?.asMutableDictionary()
                ?.also { collectionMap[index] = it }
    }

    override fun toJSON(): String {
        throw CouchbaseLiteError("Mutable objects may not be encoded as JSON")
    }
}

internal fun CBLMutableArray.asMutableArray() = MutableArray(this)
