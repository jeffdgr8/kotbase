package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLMutableDocument
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.chain
import com.udobny.kmm.ext.wrapError
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSError
import platform.Foundation.NSNumber

public actual class MutableDocument
internal constructor(override val actual: CBLMutableDocument) :
    Document(actual) {

    public actual constructor() : this(CBLMutableDocument())

    public actual constructor(id: String?) : this(CBLMutableDocument(id))

    public actual constructor(data: Map<String, Any?>) : this(
        CBLMutableDocument(data.actualIfDelegated())
    )

    public actual constructor(id: String?, data: Map<String, Any?>) : this(
        CBLMutableDocument(id, data.actualIfDelegated())
    )

    public actual constructor(id: String?, json: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            CBLMutableDocument(id, json, error)
        }
    )

    private inline fun chain(action: CBLMutableDocument.() -> Unit) = chain(actual, action)

    actual override fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual fun setData(data: Map<String, Any?>): MutableDocument = chain {
        setData(data.actualIfDelegated())
    }

    public actual fun setJSON(json: String): MutableDocument = chain {
        throwError { error ->
            setJSON(json, error)
        }
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument = chain {
        setValue(value?.actualIfDelegated(), key)
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
}
