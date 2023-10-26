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
import kotlinx.datetime.Instant
import com.couchbase.lite.MutableDocument as CBLMutableDocument

public actual class MutableDocument
internal constructor(override val actual: CBLMutableDocument) : Document(actual) {

    public actual constructor() : this(CBLMutableDocument())

    public actual constructor(id: String?) : this(CBLMutableDocument(id))

    public actual constructor(data: Map<String, Any?>) : this(CBLMutableDocument(data.actualIfDelegated()))

    public actual constructor(id: String?, data: Map<String, Any?>) : this(
        CBLMutableDocument(id, data.actualIfDelegated())
    )

    public actual constructor(id: String?, json: String) : this(CBLMutableDocument(id, json))

    actual override fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual fun setData(data: Map<String, Any?>): MutableDocument {
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        return this
    }

    public actual fun setJSON(json: String): MutableDocument {
        collectionMap.clear()
        actual.setJSON(json)
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument {
        actual.setValue(key, value?.actualIfDelegated())
        if (value is Array || value is Dictionary) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDocument {
        actual.setString(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDocument {
        actual.setNumber(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDocument {
        actual.setInt(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDocument {
        actual.setLong(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDocument {
        actual.setFloat(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDocument {
        actual.setDouble(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDocument {
        actual.setBoolean(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDocument {
        actual.setBlob(key, value?.actual)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDate(key: String, value: Instant?): MutableDocument {
        actual.setDate(key, value?.toDate())
        collectionMap.remove(key)
        return this
    }

    public actual fun setArray(key: String, value: Array?): MutableDocument {
        actual.setArray(key, value?.actual)
        if (value != null) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDocument {
        actual.setDictionary(key, value?.actual)
        if (value != null) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun remove(key: String): MutableDocument {
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
