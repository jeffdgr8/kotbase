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

import kotbase.internal.DbContext
import kotbase.internal.JsonUtils
import kotbase.internal.fleece.*
import kotlinx.cinterop.memScoped
import kotlinx.datetime.Instant
import libcblite.*

public actual class MutableDictionary
internal constructor(
    override val actual: FLMutableDict,
    dbContext: DbContext? = null
) : Dictionary(actual, dbContext), MutableDictionaryInterface {

    public actual constructor() : this(FLMutableDict_New()!!)

    public actual constructor(data: Map<String, Any?>) : this() {
        setData(data)
    }

    public actual constructor(json: String) : this(
        try {
            wrapFLError { error ->
                memScoped {
                    FLMutableDict_NewFromJSON(json.toFLString(this), error)!!
                }
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
    )

    override var dbContext: DbContext?
        get() = super.dbContext
        set(value) {
            super.dbContext = value
            addedToDbContext = if (unsavedBlobs.isNotEmpty()) {
                value?.addStreamBlobDict(this)
                true
            } else false
        }

    private var addedToDbContext = false
    private val unsavedBlobs = mutableMapOf<String, Blob>()

    private fun removeInternal(key: String) {
        collectionMap.remove(key)
        unsavedBlobs.remove(key)
        updateUnsavedBlobContext()
    }

    private fun updateUnsavedBlobContext() {
        if (unsavedBlobs.isEmpty() && addedToDbContext) {
            dbContext?.removeStreamBlobDict(this)
            addedToDbContext = false
        } else if (unsavedBlobs.isNotEmpty() && !addedToDbContext) {
            dbContext?.addStreamBlobDict(this)
            addedToDbContext = true
        }
    }

    internal fun mergeSavedBlobs() {
        unsavedBlobs.forEach { (key, value) ->
            checkNotNull(value.actual) { "Blob stream should have been saved" }
            if (getFLValue(key)?.toObject(null) != null) {
                throw IllegalStateException("Blob stream placeholder should be null")
            }
            setBlob(key, value)
        }
        unsavedBlobs.clear()
    }

    actual override fun setData(data: Map<String, Any?>): MutableDictionary {
        FLMutableDict_RemoveAll(actual)
        collectionMap.clear()
        unsavedBlobs.clear()
        data.forEach { (key, value) ->
            setValue(key, value)
        }
        return this
    }

    actual override fun setJSON(json: String): MutableDictionary {
        @Suppress("UNCHECKED_CAST")
        val data = JsonUtils.parseJson(json) as? Map<String, Any?>
            ?: error("Parsed result is not a Dictionary")
        setData(data)
        return this
    }

    actual override fun setValue(key: String, value: Any?): MutableDictionary {
        actual.setValue(key, value, dbContext)
        if (value is Array || value is Dictionary && value !== this) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        if (value is Blob && value.actual == null) {
            unsavedBlobs[key] = value
        } else {
            unsavedBlobs.remove(key)
        }
        updateUnsavedBlobContext()
        mutate()
        return this
    }

    actual override fun setString(key: String, value: String?): MutableDictionary {
        actual.setString(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setNumber(key: String, value: Number?): MutableDictionary {
        actual.setNumber(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setInt(key: String, value: Int): MutableDictionary {
        actual.setInt(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setLong(key: String, value: Long): MutableDictionary {
        actual.setLong(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setFloat(key: String, value: Float): MutableDictionary {
        actual.setFloat(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setDouble(key: String, value: Double): MutableDictionary {
        actual.setDouble(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setBoolean(key: String, value: Boolean): MutableDictionary {
        actual.setBoolean(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setBlob(key: String, value: Blob?): MutableDictionary {
        actual.setBlob(key, value, dbContext)
        collectionMap.remove(key)
        if (value != null && value.actual == null) {
            unsavedBlobs[key] = value
        } else {
            unsavedBlobs.remove(key)
        }
        updateUnsavedBlobContext()
        mutate()
        return this
    }

    actual override fun setDate(key: String, value: Instant?): MutableDictionary {
        actual.setDate(key, value)
        removeInternal(key)
        mutate()
        return this
    }

    actual override fun setArray(key: String, value: Array?): MutableDictionary {
        actual.setArray(key, value, dbContext)
        removeInternal(key)
        if (value != null) {
            collectionMap[key] = value
        }
        mutate()
        return this
    }

    actual override fun setDictionary(key: String, value: Dictionary?): MutableDictionary {
        actual.setDictionary(key, value, dbContext)
        removeInternal(key)
        if (value != null && value !== this) {
            collectionMap[key] = value
        }
        mutate()
        return this
    }

    actual override fun remove(key: String): MutableDictionary {
        actual.remove(key)
        removeInternal(key)
        mutate()
        return this
    }

    override fun getValue(key: String): Any? {
        return collectionMap[key]
            ?: getFLValue(key)?.toMutableNative(dbContext) { setValue(key, it) }
                ?.also { if (it is Array || it is Dictionary) collectionMap[key] = it }
            ?: unsavedBlobs[key]
    }

    override fun getBlob(key: String): Blob? {
        return super.getBlob(key)
            ?: unsavedBlobs[key]
    }

    actual override fun getArray(key: String): MutableArray? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toMutableArray(dbContext) { setArray(key, it) }
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): MutableDictionary? {
        return getInternalCollection(key)
            ?: getFLValue(key)?.toMutableDictionary(dbContext) { setDictionary(key, it) }
                ?.also { collectionMap[key] = it }
    }

    override fun toJSON(): String {
        throw CouchbaseLiteError("Mutable objects may not be encoded as JSON")
    }
}
