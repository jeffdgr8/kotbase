package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMutableDocument
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.chain
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class MutableDocument
internal constructor(override val actual: CBLMutableDocument) :
    Document(actual) {

    public actual constructor() : this(CBLMutableDocument())

    public actual constructor(id: String?) : this(CBLMutableDocument(id))

    public actual constructor(data: Map<String, Any?>) : this(
        CBLMutableDocument(data.actualIfDelegated())
    ) {
        setBooleans(data)
    }

    public actual constructor(id: String?, data: Map<String, Any?>) : this(
        CBLMutableDocument(id, data.actualIfDelegated())
    )

    public actual constructor(id: String?, json: String) : this(id) {
        setJSON(json)
    }

    private inline fun chain(action: CBLMutableDocument.() -> Unit) = chain(actual, action)

    private fun setBooleans(data: Map<String, Any?>) {
        data.forEach { (key, value) ->
            if (value is Boolean) {
                // Booleans treated as numbers unless explicitly using boolean API
                setBoolean(key, value)
            }
        }
    }

    actual override fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual fun setData(data: Map<String, Any?>): MutableDocument = chain {
        setData(data.actualIfDelegated())
        setBooleans(data)
    }

    public actual fun setJSON(json: String): MutableDocument = chain {
        try {
            wrapCBLError { error ->
                actual.setJSON(json, error)
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument = chain {
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> setBoolean(value, key)
            else -> setValue(value?.actualIfDelegated(), key)
        }
    }

    public actual fun setString(key: String, value: String?): MutableDocument = chain {
        setString(value, key)
    }

    public actual fun setNumber(key: String, value: Number?): MutableDocument = chain {
        setNumber(value as NSNumber?, key)
    }

    public actual fun setInt(key: String, value: Int): MutableDocument = chain {
        setInteger(value.convert(), key)
    }

    public actual fun setLong(key: String, value: Long): MutableDocument = chain {
        setLongLong(value, key)
    }

    public actual fun setFloat(key: String, value: Float): MutableDocument = chain {
        setFloat(value, key)
    }

    public actual fun setDouble(key: String, value: Double): MutableDocument = chain {
        setDouble(value, key)
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDocument = chain {
        setBoolean(value, key)
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDocument = chain {
        setBlob(value?.actual, key)
    }

    public actual fun setDate(key: String, value: Instant?): MutableDocument = chain {
        setDate(value?.toNSDate(), key)
    }

    public actual fun setArray(key: String, value: Array?): MutableDocument = chain {
        setArray(value?.actual, key)
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDocument = chain {
        setDictionary(value?.actual, key)
    }

    public actual fun remove(key: String): MutableDocument = chain {
        removeValueForKey(key)
    }

    actual override fun getArray(key: String): MutableArray? =
        actual.arrayForKey(key)?.asMutableArray()

    actual override fun getDictionary(key: String): MutableDictionary? =
        actual.dictionaryForKey(key)?.asMutableDictionary()

    override fun toJSON(): String? {
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }
}
