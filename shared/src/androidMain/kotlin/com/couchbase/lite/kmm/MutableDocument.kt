package com.couchbase.lite.kmm

import com.udobny.kmm.chain
import com.udobny.kmm.ext.*
import kotlinx.datetime.Instant

public actual class MutableDocument
internal constructor(override val actual: com.couchbase.lite.MutableDocument) : Document(actual) {

    public actual constructor() : this(com.couchbase.lite.MutableDocument())

    public actual constructor(id: String?) : this(com.couchbase.lite.MutableDocument(id))

    public actual constructor(data: Map<String, Any?>) : this(
        com.couchbase.lite.MutableDocument(data)
    )

    public actual constructor(id: String?, data: Map<String, Any?>) : this(
        com.couchbase.lite.MutableDocument(id, data)
    )

    public actual constructor(id: String?, json: String) : this(
        com.couchbase.lite.MutableDocument(id, json)
    )

    private inline fun chain(action: com.couchbase.lite.MutableDocument.() -> Unit) =
        chain(actual, action)

    actual override fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual fun setData(data: Map<String, Any?>): MutableDocument = chain {
        setData(data.toNativeDateValuesDeep())
    }

    public actual fun setJSON(json: String): MutableDocument = chain {
        setJSON(json)
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument = chain {
        setValue(key, value?.toNativeDateDeep())
    }

    public actual fun setString(key: String, value: String?): MutableDocument = chain {
        setString(key, value)
    }

    public actual fun setNumber(key: String, value: Number?): MutableDocument = chain {
        setNumber(key, value)
    }

    public actual fun setInt(key: String, value: Int): MutableDocument = chain {
        setInt(key, value)
    }

    public actual fun setLong(key: String, value: Long): MutableDocument = chain {
        setLong(key, value)
    }

    public actual fun setFloat(key: String, value: Float): MutableDocument = chain {
        setFloat(key, value)
    }

    public actual fun setDouble(key: String, value: Double): MutableDocument = chain {
        setDouble(key, value)
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDocument = chain {
        setBoolean(key, value)
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDocument = chain {
        setBlob(key, value)
    }

    public actual fun setDate(key: String, value: Instant?): MutableDocument = chain {
        setDate(key, value?.toDate())
    }

    public actual fun setArray(key: String, value: Array?): MutableDocument = chain {
        setArray(key, value?.actual)
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDocument = chain {
        setDictionary(key, value?.actual)
    }

    public actual fun remove(key: String): MutableDocument = chain {
        remove(key)
    }

    actual override fun getArray(key: String): MutableArray? =
        actual.getArray(key)?.asMutableArray()

    actual override fun getDictionary(key: String): MutableDictionary? =
        actual.getDictionary(key)?.asMutableDictionary()
}
