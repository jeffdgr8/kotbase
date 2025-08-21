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

import cocoapods.CouchbaseLite.CBLMutableDocument
import kotbase.ext.wrapCBLError
import kotlinx.cinterop.convert
import kotlin.time.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class MutableDocument
internal constructor(
    override val actual: CBLMutableDocument,
    collection: Collection? = null
) : Document(actual, collection), MutableDictionaryInterface {

    public actual constructor() : this(CBLMutableDocument())

    public actual constructor(id: String?) : this(CBLMutableDocument(id))

    public actual constructor(data: Map<String, Any?>) : this(
        CBLMutableDocument(data.removingBooleanValues().actualIfDelegated())
    ) {
        setBooleans(data)
    }

    public actual constructor(id: String?, data: Map<String, Any?>) : this(
        CBLMutableDocument(id, data.actualIfDelegated())
    )

    public actual constructor(id: String?, json: String) : this(id) {
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

    actual override fun setData(data: Map<String, Any?>): MutableDocument {
        collectionMap.clear()
        actual.setData(data.removingBooleanValues().actualIfDelegated())
        setBooleans(data)
        mutate()
        return this
    }

    actual override fun setJSON(json: String): MutableDocument {
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

    actual override fun setValue(key: String, value: Any?): MutableDocument {
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
        mutate()
        return this
    }

    actual override fun setString(key: String, value: String?): MutableDocument {
        actual.setString(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setNumber(key: String, value: Number?): MutableDocument {
        actual.setNumber(value as NSNumber?, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setInt(key: String, value: Int): MutableDocument {
        actual.setInteger(value.convert(), key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setLong(key: String, value: Long): MutableDocument {
        actual.setLongLong(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setFloat(key: String, value: Float): MutableDocument {
        actual.setFloat(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setDouble(key: String, value: Double): MutableDocument {
        actual.setDouble(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setBoolean(key: String, value: Boolean): MutableDocument {
        actual.setBoolean(value, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setBlob(key: String, value: Blob?): MutableDocument {
        actual.setBlob(value?.actual, key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setDate(key: String, value: Instant?): MutableDocument {
        actual.setDate(value?.toNSDate(), key)
        collectionMap.remove(key)
        mutate()
        return this
    }

    actual override fun setArray(key: String, value: Array?): MutableDocument {
        actual.setArray(value?.actual, key)
        if (value != null) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        mutate()
        return this
    }

    actual override fun setDictionary(key: String, value: Dictionary?): MutableDocument {
        actual.setDictionary(value?.actual, key)
        if (value != null) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        mutate()
        return this
    }

    actual override fun remove(key: String): MutableDocument {
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
