package kotbase

import kotbase.ext.toDate
import kotlinx.datetime.Instant

public actual class MutableDocument
internal constructor(override val actual: com.couchbase.lite.MutableDocument) : Document(actual) {

    public actual constructor() : this(com.couchbase.lite.MutableDocument())

    public actual constructor(id: String?) : this(com.couchbase.lite.MutableDocument(id))

    public actual constructor(data: Map<String, Any?>) : this(
        com.couchbase.lite.MutableDocument(data.actualIfDelegated())
    )

    public actual constructor(id: String?, data: Map<String, Any?>) : this(
        com.couchbase.lite.MutableDocument(id, data.actualIfDelegated())
    )

    public actual constructor(id: String?, json: String) : this(
        com.couchbase.lite.MutableDocument(id, json)
    )

    actual override fun toMutable(): MutableDocument =
        MutableDocument(actual.toMutable())

    public actual fun setData(data: Map<String, Any?>): MutableDocument {
        collectionMap.clear()
        actual.setData(data.actualIfDelegated())
        return this
    }

    public actual fun setJSON(json: String): MutableDocument {
        collectionMap.clear()
        actual.setJSON(json)
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDocument {
        actual.setValue(key, value?.actualIfDelegated())
        if (value is Array || value is Dictionary) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDocument {
        actual.setString(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDocument {
        actual.setNumber(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDocument {
        actual.setInt(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDocument {
        actual.setLong(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDocument {
        actual.setFloat(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDocument {
        actual.setDouble(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDocument {
        actual.setBoolean(key, value)
        collectionMap.remove(key)
        return this
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDocument {
        actual.setBlob(key, value?.actual)
        collectionMap.remove(key)
        return this
    }

    public actual fun setDate(key: String, value: Instant?): MutableDocument {
        actual.setDate(key, value?.toDate())
        collectionMap.remove(key)
        return this
    }

    public actual fun setArray(key: String, value: Array?): MutableDocument {
        actual.setArray(key, value?.actual)
        if (value != null) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDocument {
        actual.setDictionary(key, value?.actual)
        if (value != null) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
        return this
    }

    public actual fun remove(key: String): MutableDocument {
        actual.remove(key)
        collectionMap.remove(key)
        return this
    }

    actual override fun getArray(key: String): MutableArray? {
        return getInternalCollection(key)
            ?: actual.getArray(key)?.asMutableArray()
                ?.also { collectionMap[key] = it }
    }

    actual override fun getDictionary(key: String): MutableDictionary? {
        return getInternalCollection(key)
            ?: actual.getDictionary(key)?.asMutableDictionary()
                ?.also { collectionMap[key] = it }
    }
}
