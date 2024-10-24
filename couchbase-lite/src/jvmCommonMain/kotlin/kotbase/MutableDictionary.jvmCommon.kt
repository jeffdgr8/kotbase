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
import com.couchbase.lite.MutableDictionary as CBLMutableDictionary

public actual class MutableDictionary
internal constructor(override val actual: CBLMutableDictionary) : Dictionary(actual) {

    public actual constructor() : this(CBLMutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(CBLMutableDictionary(data.actualIfDelegated()))

    public actual constructor(json: String) : this(CBLMutableDictionary(json))

    public actual fun setData(data: Map<String, Any?>): MutableDictionary {
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        return this
    }

    public actual fun setJSON(json: String): MutableDictionary {
        collectionMap.clear()
        actual.setJSON(json)
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDictionary {
        actual.setValue(key, value?.actualIfDelegated())
        if (value is Array || value is Dictionary) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDictionary {
        actual.setString(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDictionary {
        actual.setNumber(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDictionary {
        actual.setInt(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDictionary {
        actual.setLong(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDictionary {
        actual.setFloat(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDictionary {
        actual.setDouble(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDictionary {
        actual.setBoolean(key, value)
        collectionMap.remove(key)
        return this
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setBlob(key: String, value: Blob?): MutableDictionary {
        if (value == null) {
            actual.setValue(key, null)
        } else {
            actual.setBlob(key, value.actual)
        }
        //actual.setBlob(key, value?.actual)
        collectionMap.remove(key)
        return this
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setDate(key: String, value: Instant?): MutableDictionary {
        if (value == null) {
            actual.setValue(key, null)
        } else {
            actual.setDate(key, value.toDate())
        }
        //actual.setDate(key, value?.toDate())
        collectionMap.remove(key)
        return this
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setArray(key: String, value: Array?): MutableDictionary {
        if (value == null) {
            actual.setValue(key, null)
            collectionMap.remove(key)
        } else {
            actual.setArray(key, value.actual)
            collectionMap[key] = value
        }
        //actual.setArray(key, value?.actual)
        return this
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setDictionary(key: String, value: Dictionary?): MutableDictionary {
        if (value == null) {
            actual.setValue(key, null)
            collectionMap.remove(key)
        } else {
            actual.setDictionary(key, value.actual)
            collectionMap[key] = value
        }
        //actual.setDictionary(key, value?.actual)
        return this
    }

    public actual fun remove(key: String): MutableDictionary {
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
