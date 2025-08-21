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
import com.couchbase.lite.MutableDictionary as CBLMutableDictionary

public actual class MutableDictionary
internal constructor(override val actual: CBLMutableDictionary) : Dictionary(actual), MutableDictionaryInterface {

    public actual constructor() : this(CBLMutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(CBLMutableDictionary(data.actualIfDelegated()))

    public actual constructor(json: String) : this(CBLMutableDictionary(json))

    actual override fun setData(data: Map<String, Any?>): MutableDictionary {
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        return this
    }

    actual override fun setJSON(json: String): MutableDictionary {
        collectionMap.clear()
        actual.setJSON(json)
        return this
    }

    actual override fun setValue(key: String, value: Any?): MutableDictionary {
        actual.setValue(key, value?.actualIfDelegated())
        if (value is Array || value is Dictionary && value !== this) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    actual override fun setString(key: String, value: String?): MutableDictionary {
        actual.setString(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setNumber(key: String, value: Number?): MutableDictionary {
        actual.setNumber(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setInt(key: String, value: Int): MutableDictionary {
        actual.setInt(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setLong(key: String, value: Long): MutableDictionary {
        actual.setLong(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setFloat(key: String, value: Float): MutableDictionary {
        actual.setFloat(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setDouble(key: String, value: Double): MutableDictionary {
        actual.setDouble(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setBoolean(key: String, value: Boolean): MutableDictionary {
        actual.setBoolean(key, value)
        collectionMap.remove(key)
        return this
    }

    actual override fun setBlob(key: String, value: Blob?): MutableDictionary {
        actual.setBlob(key, value?.actual)
        collectionMap.remove(key)
        return this
    }

    actual override fun setDate(key: String, value: Instant?): MutableDictionary {
        actual.setDate(key, value?.toDate())
        collectionMap.remove(key)
        return this
    }

    actual override fun setArray(key: String, value: Array?): MutableDictionary {
        actual.setArray(key, value?.actual)
        if (value == null) {
            collectionMap.remove(key)
        } else {
            collectionMap[key] = value
        }
        return this
    }

    actual override fun setDictionary(key: String, value: Dictionary?): MutableDictionary {
        actual.setDictionary(key, value?.actual)
        if (value != null && value !== this) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    actual override fun remove(key: String): MutableDictionary {
        actual.remove(key)
        collectionMap.remove(key)
        return this
    }

    actual override fun getArray(key: String): MutableArray? {
        return getInternalCollection(key)
            ?: actual.getArray(key)?.asMutableArray()
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): MutableDictionary? {
        return getInternalCollection(key)
            ?: actual.getDictionary(key)?.asMutableDictionary()
                ?.also { collectionMap[key] = it }
    }
}

internal fun CBLMutableDictionary.asMutableDictionary() =
    MutableDictionary(this)
