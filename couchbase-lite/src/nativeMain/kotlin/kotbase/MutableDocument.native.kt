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

import cnames.structs.CBLDocument
import kotbase.internal.fleece.*
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import kotlinx.datetime.Instant
import libcblite.*

public actual class MutableDocument
internal constructor(
    actual: CPointer<CBLDocument>,
    database: Database? = null
) : Document(actual, database) {

    public actual constructor() : this(CBLDocument_Create()!!) {
        CBLDocument_Release(actual)
    }

    public actual constructor(id: String?) : this(
        memScoped {
            CBLDocument_CreateWithID(id.toFLString(this))!!
        }
    ) {
        CBLDocument_Release(actual)
    }

    public actual constructor(data: Map<String, Any?>) : this() {
        setData(data)
    }

    public actual constructor(id: String?, data: Map<String, Any?>) : this(id) {
        setData(data)
    }

    public actual constructor(id: String?, json: String) : this(id) {
        setJSON(json)
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
            dbContext.removeStreamBlobDoc(this)
            addedToDbContext = false
        } else if (unsavedBlobs.isNotEmpty() && !addedToDbContext) {
            dbContext.addStreamBlobDoc(this)
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

    override var properties: FLMutableDict = CBLDocument_MutableProperties(actual)!!
        set(value) {
            super.properties = value
            field = value
            unsavedBlobs.clear()
        }

    public actual fun setData(data: Map<String, Any?>): MutableDocument {
        properties = MutableDictionary(data).actual
        return this
    }

    public actual fun setJSON(json: String): MutableDocument {
        if (!json.startsWith("{")) {
            throw IllegalArgumentException("JSON is not a Dictionary")
        }
        try {
            wrapCBLError { error ->
                memScoped {
                    CBLDocument_SetJSON(actual, json.toFLString(this), error)
                }
            }
            properties = CBLDocument_MutableProperties(actual)!!
        } catch (e: CouchbaseLiteException) {
            if (e.code == CBLError.Code.INVALID_QUERY) {
                throw IllegalArgumentException("Failed parsing JSON", e)
            }
        }
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument {
        properties.setValue(key, value, dbContext)
        if (value is Array || value is Dictionary) {
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
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDocument {
        properties.setString(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDocument {
        properties.setNumber(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDocument {
        properties.setInt(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDocument {
        properties.setLong(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDocument {
        properties.setFloat(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDocument {
        properties.setDouble(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDocument {
        properties.setBoolean(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDocument {
        properties.setBlob(key, value, dbContext)
        collectionMap.remove(key)
        if (value != null && value.actual == null) {
            unsavedBlobs[key] = value
        } else {
            unsavedBlobs.remove(key)
        }
        updateUnsavedBlobContext()
        return this
    }

    public actual fun setDate(key: String, value: Instant?): MutableDocument {
        properties.setDate(key, value)
        removeInternal(key)
        return this
    }

    public actual fun setArray(key: String, value: Array?): MutableDocument {
        properties.setArray(key, value, dbContext)
        removeInternal(key)
        if (value != null) {
            collectionMap[key] = value
        }
        return this
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDocument {
        properties.setDictionary(key, value, dbContext)
        removeInternal(key)
        if (value != null) {
            collectionMap[key] = value
        }
        return this
    }

    public actual fun remove(key: String): MutableDocument {
        properties.remove(key)
        removeInternal(key)
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

    override fun toJSON(): String? {
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }
}
