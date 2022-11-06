@file:JvmName("MutableDictionaryJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

import com.udobny.kmp.chain
import com.udobny.kmp.ext.toDate
import kotlinx.datetime.Instant

public actual class MutableDictionary
internal constructor(override val actual: com.couchbase.lite.MutableDictionary) :
    Dictionary(actual) {

    public actual constructor() : this(com.couchbase.lite.MutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(
        com.couchbase.lite.MutableDictionary(data.actualIfDelegated())
    )

    public actual constructor(json: String) : this(com.couchbase.lite.MutableDictionary(json))

    private inline fun chain(action: com.couchbase.lite.MutableDictionary.() -> Unit) =
        chain(actual, action)

    public actual fun setData(data: Map<String, Any?>): MutableDictionary = chain {
        collectionMap.clear()
        setData(data.actualIfDelegated())
    }

    public actual fun setJSON(json: String): MutableDictionary = chain {
        collectionMap.clear()
        setJSON(json)
    }

    public actual fun setValue(key: String, value: Any?): MutableDictionary = chain {
        setValue(key, value?.actualIfDelegated())
        if (value is Array || value is Dictionary) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
    }

    public actual fun setString(key: String, value: String?): MutableDictionary = chain {
        setString(key, value)
        collectionMap.remove(key)
    }

    public actual fun setNumber(key: String, value: Number?): MutableDictionary = chain {
        setNumber(key, value)
        collectionMap.remove(key)
    }

    public actual fun setInt(key: String, value: Int): MutableDictionary = chain {
        setInt(key, value)
        collectionMap.remove(key)
    }

    public actual fun setLong(key: String, value: Long): MutableDictionary = chain {
        setLong(key, value)
        collectionMap.remove(key)
    }

    public actual fun setFloat(key: String, value: Float): MutableDictionary = chain {
        setFloat(key, value)
        collectionMap.remove(key)
    }

    public actual fun setDouble(key: String, value: Double): MutableDictionary = chain {
        setDouble(key, value)
        collectionMap.remove(key)
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDictionary = chain {
        setBoolean(key, value)
        collectionMap.remove(key)
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setBlob(key: String, value: Blob?): MutableDictionary = chain {
        if (value == null) {
            setValue(key, null)
        } else {
            setBlob(key, value.actual)
        }
        //setBlob(key, value?.actual)
        collectionMap.remove(key)
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setDate(key: String, value: Instant?): MutableDictionary = chain {
        if (value == null) {
            setValue(key, null)
        } else {
            setDate(key, value.toDate())
        }
        //setDate(key, value?.toDate())
        collectionMap.remove(key)
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setArray(key: String, value: Array?): MutableDictionary = chain {
        if (value == null) {
            setValue(key, null)
            collectionMap.remove(key)
        } else {
            setArray(key, value.actual)
            collectionMap[key] = value
        }
        //setArray(key, value?.actual)
    }

    // TODO: Remove setValue() when nullable in 3.1
    //  https://forums.couchbase.com/t/couchbase-lite-java-sdk-api-feedback/33897/1/
    public actual fun setDictionary(key: String, value: Dictionary?): MutableDictionary = chain {
        if (value == null) {
            setValue(key, null)
            collectionMap.remove(key)
        } else {
            setDictionary(key, value.actual)
            collectionMap[key] = value
        }
        //setDictionary(key, value?.actual)
    }

    public actual fun remove(key: String): MutableDictionary = chain {
        remove(key)
        collectionMap.remove(key)
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

internal fun com.couchbase.lite.MutableDictionary.asMutableDictionary() =
    MutableDictionary(this)
