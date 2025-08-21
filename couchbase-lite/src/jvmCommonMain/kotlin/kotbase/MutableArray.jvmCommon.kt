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

import kotbase.ext.toDate
import kotlin.time.Instant
import com.couchbase.lite.MutableArray as CBLMutableArray

public actual class MutableArray
internal constructor(override val actual: CBLMutableArray) : Array(actual), MutableArrayInterface {

    public actual constructor() : this(CBLMutableArray())

    public actual constructor(data: List<Any?>) : this(CBLMutableArray(data.actualIfDelegated()))

    public actual constructor(json: String) : this(CBLMutableArray(json))

    actual override fun setData(data: List<Any?>): MutableArray {
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        return this
    }

    actual override fun setJSON(json: String): MutableArray {
        collectionMap.clear()
        actual.setJSON(json)
        return this
    }

    actual override fun setValue(index: Int, value: Any?): MutableArray {
        actual.setValue(index, value?.actualIfDelegated())
        if (value is Array && value !== this || value is Dictionary) {
            collectionMap[index] = value
        } else {
            collectionMap.remove(index)
        }
        return this
    }

    actual override fun setString(index: Int, value: String?): MutableArray {
        actual.setString(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setNumber(index: Int, value: Number?): MutableArray {
        actual.setNumber(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setInt(index: Int, value: Int): MutableArray {
        actual.setInt(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setLong(index: Int, value: Long): MutableArray {
        actual.setLong(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setFloat(index: Int, value: Float): MutableArray {
        actual.setFloat(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setDouble(index: Int, value: Double): MutableArray {
        actual.setDouble(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setBoolean(index: Int, value: Boolean): MutableArray {
        actual.setBoolean(index, value)
        collectionMap.remove(index)
        return this
    }

    actual override fun setBlob(index: Int, value: Blob?): MutableArray {
        actual.setBlob(index, value?.actual)
        collectionMap.remove(index)
        return this
    }

    actual override fun setDate(index: Int, value: Instant?): MutableArray {
        actual.setDate(index, value?.toDate())
        collectionMap.remove(index)
        return this
    }

    actual override fun setArray(index: Int, value: Array?): MutableArray {
        actual.setArray(index, value?.actual)
        if (value != null && value !== this) {
            collectionMap[index] = value
        } else {
            collectionMap.remove(index)
        }
        return this
    }

    actual override fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        actual.setDictionary(index, value?.actual)
        if (value == null) {
            collectionMap.remove(index)
        } else {
            collectionMap[index] = value
        }
        return this
    }

    actual override fun addValue(value: Any?): MutableArray {
        actual.addValue(value?.actualIfDelegated())
        if (value is Array && value !== this || value is Dictionary) {
            collectionMap[count - 1] = value
        }
        return this
    }

    actual override fun addString(value: String?): MutableArray {
        actual.addString(value)
        return this
    }

    actual override fun addNumber(value: Number?): MutableArray {
        actual.addNumber(value)
        return this
    }

    actual override fun addInt(value: Int): MutableArray {
        actual.addInt(value)
        return this
    }

    actual override fun addLong(value: Long): MutableArray {
        actual.addLong(value)
        return this
    }

    actual override fun addFloat(value: Float): MutableArray {
        actual.addFloat(value)
        return this
    }

    actual override fun addDouble(value: Double): MutableArray {
        actual.addDouble(value)
        return this
    }

    actual override fun addBoolean(value: Boolean): MutableArray {
        actual.addBoolean(value)
        return this
    }

    actual override fun addBlob(value: Blob?): MutableArray {
        actual.addBlob(value?.actual)
        return this
    }

    actual override fun addDate(value: Instant?): MutableArray {
        actual.addDate(value?.toDate())
        return this
    }

    actual override fun addArray(value: Array?): MutableArray {
        actual.addArray(value?.actual)
        if (value != null && value !== this) {
            collectionMap[count - 1] = value
        }
        return this
    }

    actual override fun addDictionary(value: Dictionary?): MutableArray {
        actual.addDictionary(value?.actual)
        if (value != null) {
            collectionMap[count - 1] = value
        }
        return this
    }

    actual override fun insertValue(index: Int, value: Any?): MutableArray {
        actual.insertValue(index, value?.actualIfDelegated())
        incrementAfter(index, collectionMap)
        if (value is Array && value !== this || value is Dictionary) {
            collectionMap[index] = value
        }
        return this
    }

    actual override fun insertString(index: Int, value: String?): MutableArray {
        actual.insertString(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertNumber(index: Int, value: Number?): MutableArray {
        actual.insertNumber(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertInt(index: Int, value: Int): MutableArray {
        actual.insertInt(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertLong(index: Int, value: Long): MutableArray {
        actual.insertLong(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertFloat(index: Int, value: Float): MutableArray {
        actual.insertFloat(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertDouble(index: Int, value: Double): MutableArray {
        actual.insertDouble(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertBoolean(index: Int, value: Boolean): MutableArray {
        actual.insertBoolean(index, value)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertBlob(index: Int, value: Blob?): MutableArray {
        actual.insertBlob(index, value?.actual)
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertDate(index: Int, value: Instant?): MutableArray {
        actual.insertDate(index, value?.toDate())
        incrementAfter(index, collectionMap)
        return this
    }

    actual override fun insertArray(index: Int, value: Array?): MutableArray {
        actual.insertArray(index, value?.actual)
        incrementAfter(index, collectionMap)
        if (value != null && value !== this) {
            collectionMap[index] = value
        }
        return this
    }

    actual override fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        actual.insertDictionary(index, value?.actual)
        incrementAfter(index, collectionMap)
        if (value != null) {
            collectionMap[index] = value
        }
        return this
    }

    actual override fun remove(index: Int): MutableArray {
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

internal fun CBLMutableArray.asMutableArray() = MutableArray(this)
