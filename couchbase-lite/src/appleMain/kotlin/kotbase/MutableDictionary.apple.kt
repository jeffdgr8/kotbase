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

import cocoapods.CouchbaseLite.CBLMutableDictionary
import kotbase.ext.wrapCBLError
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class MutableDictionary
internal constructor(override val actual: CBLMutableDictionary) : Dictionary(actual), MutableDictionaryInterface {

    public actual constructor() : this(CBLMutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(
        CBLMutableDictionary(data.removingBooleanValues().actualIfDelegated())
    ) {
        setBooleans(data)
    }

    public actual constructor(json: String) : this() {
        setJSON(json)
    }

    private fun setBooleans(data: Map<String, Any?>) {
        data.forEach { (key, value) ->
            if (value is Boolean) {
                // Booleans treated as numbers unless explicitly using boolean API
                setBoolean(key, value)
            }
        }
    }

    actual override fun setData(data: Map<String, Any?>): MutableDictionary {
        collectionMap.clear()
        actual.setData(data.removingBooleanValues().actualIfDelegated())
        setBooleans(data)
        mutate()
        return this
    }

    actual override fun setJSON(json: String): MutableDictionary {
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

    actual override fun setValue(key: String, value: Any?): MutableDictionary {
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.setBoolean(value, key)
            else -> actual.setValue(value?.actualIfDelegated(), key)
        }
        if (value is Array || value is Dictionary && value !== this) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        mutate()
        return this
    }

    actual override fun setString(key: String, value: String?): MutableDictionary {
        actual.setString(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setNumber(key: String, value: Number?): MutableDictionary {
        actual.setNumber(value as NSNumber?, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setInt(key: String, value: Int): MutableDictionary {
        actual.setInteger(value.convert(), key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setLong(key: String, value: Long): MutableDictionary {
        actual.setLongLong(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setFloat(key: String, value: Float): MutableDictionary {
        actual.setFloat(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setDouble(key: String, value: Double): MutableDictionary {
        actual.setDouble(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setBoolean(key: String, value: Boolean): MutableDictionary {
        actual.setBoolean(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setBlob(key: String, value: Blob?): MutableDictionary {
        actual.setBlob(value?.actual, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setDate(key: String, value: Instant?): MutableDictionary {
        actual.setDate(value?.toNSDate(), key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setArray(key: String, value: Array?): MutableDictionary {
        actual.setArray(value?.actual, key)
        if (value == null) {
            collectionMap.remove(key)
        } else {
            collectionMap[key] = value
        }
        mutate()
        return this
    }

    actual override fun setDictionary(key: String, value: Dictionary?): MutableDictionary {
        actual.setDictionary(value?.actual, key)
        if (value != null && value !== this) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        mutate()
        return this
    }

    actual override fun remove(key: String): MutableDictionary {
        actual.removeValueForKey(key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun getArray(key: String): MutableArray? {
        return getInternalCollection(key)
            ?: actual.arrayForKey(key)?.asMutableArray()
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): MutableDictionary? {
        return getInternalCollection(key)
            ?: actual.dictionaryForKey(key)?.asMutableDictionary()
                ?.also { collectionMap[key] = it }
    }

    override fun toJSON(): String {
        throw CouchbaseLiteError("Mutable objects may not be encoded as JSON")
    }
}

internal fun CBLMutableDictionary.asMutableDictionary() = MutableDictionary(this)
