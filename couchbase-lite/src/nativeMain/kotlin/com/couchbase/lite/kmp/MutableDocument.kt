package com.couchbase.lite.kmp

import cnames.structs.CBLDocument
import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.setString
import com.couchbase.lite.kmp.internal.fleece.setValue
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.wrapCBLError
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

    public actual constructor(id: String?) : this(CBLDocument_CreateWithID(id.toFLString())!!) {
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

    override val properties: FLMutableDict
        get() = CBLDocument_MutableProperties(actual)!!

    public actual fun setData(data: Map<String, Any?>): MutableDocument {
        CBLDocument_SetProperties(actual, MutableDictionary(data).actual)
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
        } catch (e: CouchbaseLiteException) {
            if (e.getCode() == CBLError.Code.INVALID_QUERY) {
                throw IllegalArgumentException("Failed parsing JSON", e)
            }
        }
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument {
        properties.setValue(key, value, dbContext)
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDocument {
        properties.setString(key, value)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDocument {
        properties.setNumber(key, value)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDocument {
        properties.setInt(key, value)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDocument {
        properties.setLong(key, value)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDocument {
        properties.setFloat(key, value)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDocument {
        properties.setDouble(key, value)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDocument {
        properties.setBoolean(key, value)
        return this
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDocument {
        properties.setBlob(key, value, dbContext)
        return this
    }

    public actual fun setDate(key: String, value: Instant?): MutableDocument {
        properties.setDate(key, value)
        return this
    }

    public actual fun setArray(key: String, value: Array?): MutableDocument {
        properties.setArray(key, value, dbContext)
        return this
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDocument {
        properties.setDictionary(key, value, dbContext)
        return this
    }

    public actual fun remove(key: String): MutableDocument {
        properties.remove(key)
        return this
    }

    override fun getValue(key: String): Any? =
        getFLValue(key)?.toMutableNative(dbContext) { setValue(key, it) }

    actual override fun getArray(key: String): MutableArray? =
        getFLValue(key)?.toMutableArray(dbContext) { setArray(key, it) }

    actual override fun getDictionary(key: String): MutableDictionary? =
        getFLValue(key)?.toMutableDictionary(dbContext) { setDictionary(key, it) }

    override fun toJSON(): String? {
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }

    override val isMutable: Boolean = true
}
