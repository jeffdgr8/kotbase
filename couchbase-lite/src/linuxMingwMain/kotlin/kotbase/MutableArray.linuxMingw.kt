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

import kotbase.ext.toStringMillis
import kotbase.internal.DbContext
import kotbase.internal.JsonUtils
import kotbase.internal.fleece.*
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.datetime.Instant
import libcblite.*

public actual class MutableArray
internal constructor(
    override val actual: FLMutableArray,
    dbContext: DbContext? = null
) : Array(actual, dbContext) {

    public actual constructor() : this(FLMutableArray_New()!!) {
        FLMutableArray_Release(actual)
    }

    public actual constructor(data: List<Any?>) : this() {
        setData(data)
    }

    public actual constructor(json: String) : this(
        try {
            wrapFLError { error ->
                memScoped {
                    FLMutableArray_NewFromJSON(json.toFLString(this), error)!!
                }
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
    ) {
        FLMutableArray_Release(actual)
    }

    override var dbContext: DbContext?
        get() = super.dbContext
        set(value) {
            super.dbContext = value
            if (unsavedBlobs.isNotEmpty()) {
                value?.addStreamBlobArray(this)
            }
        }

    private var addedToDbContext = false
    private val unsavedBlobs = mutableMapOf<Int, Blob>()

    private fun removeInternal(index: Int) {
        collectionMap.remove(index)
        unsavedBlobs.remove(index)
        updateUnsavedBlobContext()
    }

    private fun updateUnsavedBlobContext() {
        if (unsavedBlobs.isEmpty() && addedToDbContext) {
            dbContext?.removeStreamBlobArray(this)
            addedToDbContext = false
        } else if (unsavedBlobs.isNotEmpty() && !addedToDbContext) {
            dbContext?.addStreamBlobArray(this)
            addedToDbContext = true
        }
    }

    internal fun mergeSavedBlobs() {
        unsavedBlobs.forEach { (index, value) ->
            checkNotNull(value.actual) { "Blob stream should have been saved" }
            if (getFLValue(index)?.toObject(null) != null) {
                throw IllegalStateException("Blob stream placeholder should be null")
            }
            setBlob(index, value)
        }
        unsavedBlobs.clear()
    }

    public actual fun setData(data: List<Any?>): MutableArray {
        FLMutableArray_Resize(actual, data.size.convert())
        unsavedBlobs.clear()
        data.forEachIndexed { index, value ->
            setValue(index, value)
        }
        return this
    }

    public actual fun setJSON(json: String): MutableArray {
        val data = JsonUtils.parseJson(json) as? List<Any?>
            ?: error("Parsed result is not an Array")
        setData(data)
        return this
    }

    public actual fun setValue(index: Int, value: Any?): MutableArray {
        // invalid type error expected to supersede index out of bounds
        when (value) {
            is Boolean, is ByteArray, is Blob, is String, is Instant, is Number,
            is List<*>, is Array, is Map<*, *>, is Dictionary, null -> Unit
            else -> invalidTypeError(value)
        }
        checkIndex(index)
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Boolean -> setBoolean(index, value)
            is ByteArray -> setBlob(index, Blob(value))
            is Blob -> setBlob(index, value)
            is String -> setString(index, value)
            is Instant -> setDate(index, value)
            is Number -> setNumber(index, value)
            is List<*> -> setArray(index, MutableArray(value))
            is Array -> setArray(index, value)
            is Map<*, *> -> setDictionary(index, MutableDictionary(value as Map<String, Any?>))
            is Dictionary -> setDictionary(index, value)
            null -> FLMutableArray_SetNull(actual, index.convert())
            else -> invalidTypeError(value)
        }
        return this
    }

    public actual fun setString(index: Int, value: String?): MutableArray {
        checkIndex(index)
        if (value != null) {
            memScoped {
                FLMutableArray_SetString(actual, index.convert(), value.toFLString(this))
            }
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        removeInternal(index)
        return this
    }

    public actual fun setNumber(index: Int, value: Number?): MutableArray {
        checkIndex(index)
        when (value) {
            is Double -> FLMutableArray_SetDouble(actual, index.convert(), value)
            is Float -> FLMutableArray_SetFloat(actual, index.convert(), value)
            null -> FLMutableArray_SetNull(actual, index.convert())
            else -> FLMutableArray_SetInt(actual, index.convert(), value.toLong().convert())
        }
        removeInternal(index)
        return this
    }

    public actual fun setInt(index: Int, value: Int): MutableArray {
        checkIndex(index)
        FLMutableArray_SetInt(actual, index.convert(), value.convert())
        removeInternal(index)
        return this
    }

    public actual fun setLong(index: Int, value: Long): MutableArray {
        checkIndex(index)
        FLMutableArray_SetInt(actual, index.convert(), value.convert())
        removeInternal(index)
        return this
    }

    public actual fun setFloat(index: Int, value: Float): MutableArray {
        checkIndex(index)
        FLMutableArray_SetFloat(actual, index.convert(), value)
        removeInternal(index)
        return this
    }

    public actual fun setDouble(index: Int, value: Double): MutableArray {
        checkIndex(index)
        FLMutableArray_SetDouble(actual, index.convert(), value)
        removeInternal(index)
        return this
    }

    public actual fun setBoolean(index: Int, value: Boolean): MutableArray {
        checkIndex(index)
        FLMutableArray_SetBool(actual, index.convert(), value)
        removeInternal(index)
        return this
    }

    public actual fun setBlob(index: Int, value: Blob?): MutableArray {
        checkIndex(index)
        collectionMap.remove(index)
        unsavedBlobs.remove(index)
        if (value?.actual == null) {
            FLMutableArray_SetNull(actual, index.convert())
            if (value != null) {
                unsavedBlobs[index] = value
            }
        } else {
            FLMutableArray_SetBlob(actual, index.convert(), value.actual)
        }
        value?.checkSetDb(dbContext)
        updateUnsavedBlobContext()
        return this
    }

    public actual fun setDate(index: Int, value: Instant?): MutableArray {
        checkIndex(index)
        if (value != null) {
            memScoped {
                FLMutableArray_SetString(actual, index.convert(), value.toStringMillis().toFLString(this))
            }
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        removeInternal(index)
        return this
    }

    public actual fun setArray(index: Int, value: Array?): MutableArray {
        checkIndex(index)
        removeInternal(index)
        if (value != null) {
            checkSelf(value.actual)
            FLMutableArray_SetArray(actual, index.convert(), value.actual)
            collectionMap[index] = value
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun setDictionary(index: Int, value: Dictionary?): MutableArray {
        checkIndex(index)
        removeInternal(index)
        if (value != null) {
            FLMutableArray_SetDict(actual, index.convert(), value.actual)
            collectionMap[index] = value
        } else {
            FLMutableArray_SetNull(actual, index.convert())
        }
        return this
    }

    public actual fun addValue(value: Any?): MutableArray {
        @Suppress("UNCHECKED_CAST")
        when (value) {
            is Boolean -> addBoolean(value)
            is ByteArray -> addBlob(Blob(value))
            is Blob -> addBlob(value)
            is String -> addString(value)
            is Instant -> addDate(value)
            is Number -> addNumber(value)
            is List<*> -> addArray(MutableArray(value))
            is Array -> addArray(value)
            is Map<*, *> -> addDictionary(MutableDictionary(value as Map<String, Any?>))
            is Dictionary -> addDictionary(value)
            null -> FLMutableArray_AppendNull(actual)
            else -> invalidTypeError(value)
        }
        return this
    }

    public actual fun addString(value: String?): MutableArray {
        if (value != null) {
            memScoped {
                FLMutableArray_AppendString(actual, value.toFLString(this))
            }
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addNumber(value: Number?): MutableArray {
        when (value) {
            is Double -> FLMutableArray_AppendDouble(actual, value)
            is Float -> FLMutableArray_AppendFloat(actual, value)
            null -> FLMutableArray_AppendNull(actual)
            else -> FLMutableArray_AppendInt(actual, value.toLong().convert())
        }
        return this
    }

    public actual fun addInt(value: Int): MutableArray {
        FLMutableArray_AppendInt(actual, value.convert())
        return this
    }

    public actual fun addLong(value: Long): MutableArray {
        FLMutableArray_AppendInt(actual, value.convert())
        return this
    }

    public actual fun addFloat(value: Float): MutableArray {
        FLMutableArray_AppendFloat(actual, value)
        return this
    }

    public actual fun addDouble(value: Double): MutableArray {
        FLMutableArray_AppendDouble(actual, value)
        return this
    }

    public actual fun addBoolean(value: Boolean): MutableArray {
        FLMutableArray_AppendBool(actual, value)
        return this
    }

    public actual fun addBlob(value: Blob?): MutableArray {
        if (value?.actual == null) {
            FLMutableArray_AppendNull(actual)
            if (value != null) {
                unsavedBlobs[count - 1] = value
            }
        } else {
            FLMutableArray_AppendBlob(actual, value.actual)
        }
        value?.checkSetDb(dbContext)
        return this
    }

    public actual fun addDate(value: Instant?): MutableArray {
        if (value != null) {
            memScoped {
                FLMutableArray_AppendString(actual, value.toStringMillis().toFLString(this))
            }
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addArray(value: Array?): MutableArray {
        if (value != null) {
            checkSelf(value.actual)
            FLMutableArray_AppendArray(actual, value.actual)
            collectionMap[count - 1] = value
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    public actual fun addDictionary(value: Dictionary?): MutableArray {
        if (value != null) {
            FLMutableArray_AppendDict(actual, value.actual)
            collectionMap[count - 1] = value
        } else {
            FLMutableArray_AppendNull(actual)
        }
        return this
    }

    private fun insertAt(index: Int) {
        checkInsertIndex(index)
        FLMutableArray_Insert(actual, index.convert(), 1.convert())
        incrementAfter(index, collectionMap)
        incrementAfter(index, unsavedBlobs)
    }

    public actual fun insertValue(index: Int, value: Any?): MutableArray {
        insertAt(index)
        setValue(index, value)
        return this
    }

    public actual fun insertString(index: Int, value: String?): MutableArray {
        insertAt(index)
        setString(index, value)
        return this
    }

    public actual fun insertNumber(index: Int, value: Number?): MutableArray {
        insertAt(index)
        setNumber(index, value)
        return this
    }

    public actual fun insertInt(index: Int, value: Int): MutableArray {
        insertAt(index)
        setInt(index, value)
        return this
    }

    public actual fun insertLong(index: Int, value: Long): MutableArray {
        insertAt(index)
        setLong(index, value)
        return this
    }

    public actual fun insertFloat(index: Int, value: Float): MutableArray {
        insertAt(index)
        setFloat(index, value)
        return this
    }

    public actual fun insertDouble(index: Int, value: Double): MutableArray {
        insertAt(index)
        setDouble(index, value)
        return this
    }

    public actual fun insertBoolean(index: Int, value: Boolean): MutableArray {
        insertAt(index)
        setBoolean(index, value)
        return this
    }

    public actual fun insertBlob(index: Int, value: Blob?): MutableArray {
        insertAt(index)
        setBlob(index, value)
        return this
    }

    public actual fun insertDate(index: Int, value: Instant?): MutableArray {
        insertAt(index)
        setDate(index, value)
        return this
    }

    public actual fun insertArray(index: Int, value: Array?): MutableArray {
        insertAt(index)
        setArray(index, value)
        return this
    }

    public actual fun insertDictionary(index: Int, value: Dictionary?): MutableArray {
        insertAt(index)
        setDictionary(index, value)
        return this
    }

    public actual fun remove(index: Int): MutableArray {
        checkIndex(index)
        FLMutableArray_Remove(actual, index.convert(), 1.convert())
        removeInternal(index)
        return this
    }

    override fun getValue(index: Int): Any? {
        return collectionMap[index]
            ?: getFLValue(index)?.toMutableNative(dbContext) { setValue(index, it) }
                ?.also { if (it is Array || it is Dictionary) collectionMap[index] = it }
            ?: unsavedBlobs[index]
    }

    override fun getBlob(index: Int): Blob? {
        return super.getBlob(index)
            ?: unsavedBlobs[index]
    }

    actual override fun getArray(index: Int): MutableArray? {
        return getInternalCollection(index)
            ?: getFLValue(index)?.toMutableArray(dbContext) { setArray(index, it) }
                ?.also { collectionMap[index] = it }
    }

    actual override fun getDictionary(index: Int): MutableDictionary? {
        return getInternalCollection(index)
            ?: getFLValue(index)?.toMutableDictionary(dbContext) { setDictionary(index, it) }
                ?.also { collectionMap[index] = it }
    }

    override fun toJSON(): String {
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }

    private fun checkSelf(value: FLMutableArray) {
        if (value === actual) {
            throw IllegalArgumentException("Arrays cannot ba added to themselves")
        }
    }
}
