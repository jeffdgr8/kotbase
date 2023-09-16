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
internal constructor(actual: CBLMutableDictionary) : Dictionary(actual) {

    public actual constructor() : this(CBLMutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(
        CBLMutableDictionary(data.actualIfDelegated())
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

    public actual fun setData(data: Map<String, Any?>): MutableDictionary {
        data.forEach { checkSelf(it.value) }
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        setBooleans(data)
        return this
    }

    public actual fun setJSON(json: String): MutableDictionary {
        collectionMap.clear()
        try {
            wrapCBLError { error ->
                actual.setJSON(json, error)
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDictionary {
        checkSelf(value)
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> actual.setBoolean(value, key)
            else -> actual.setValue(value?.actualIfDelegated(), key)
        }
        if (value is Array || value is Dictionary) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDictionary {
        actual.setString(value, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDictionary {
        actual.setNumber(value as NSNumber?, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDictionary {
        actual.setInteger(value.convert(), key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDictionary {
        actual.setLongLong(value, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDictionary {
        actual.setFloat(value, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDictionary {
        actual.setDouble(value, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDictionary {
        actual.setBoolean(value, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDictionary {
        actual.setBlob(value?.actual, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDate(key: String, value: Instant?): MutableDictionary {
        actual.setDate(value?.toNSDate(), key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setArray(key: String, value: Array?): MutableDictionary {
        actual.setArray(value?.actual, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDictionary {
        checkSelf(value)
        actual.setDictionary(value?.actual, key)
        collectionMap.remove(key)
        return this
    }

    public actual fun remove(key: String): MutableDictionary {
        actual.removeValueForKey(key)
        collectionMap.remove(key)
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
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }

    // Java performs this check, but Objective-C does not
    private fun checkSelf(value: Any?) {
        if (value === this) {
            throw IllegalArgumentException("Dictionaries cannot ba added to themselves")
        }
    }
}

internal val MutableDictionary.actual: CBLMutableDictionary
    get() = platformState.actual as CBLMutableDictionary

internal fun CBLMutableDictionary.asMutableDictionary() = MutableDictionary(this)
