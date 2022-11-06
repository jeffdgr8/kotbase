package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLMutableDictionary
import com.couchbase.lite.kmp.ext.wrapCBLError
import com.udobny.kmp.chain
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class MutableDictionary
internal constructor(override val actual: CBLMutableDictionary) : Dictionary(actual) {

    public actual constructor() : this(CBLMutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(
        CBLMutableDictionary(data.actualIfDelegated())
    ) {
        setBooleans(data)
    }

    public actual constructor(json: String) : this() {
        setJSON(json)
    }

    private inline fun chain(action: CBLMutableDictionary.() -> Unit) = chain(actual, action)

    private fun setBooleans(data: Map<String, Any?>) {
        data.forEach { (key, value) ->
            if (value is Boolean) {
                // Booleans treated as numbers unless explicitly using boolean API
                setBoolean(key, value)
            }
        }
    }

    public actual fun setData(data: Map<String, Any?>): MutableDictionary = chain {
        data.forEach { checkSelf(it.value) }
        collectionMap.clear()
        setData(data.actualIfDelegated())
        setBooleans(data)
    }

    public actual fun setJSON(json: String): MutableDictionary = chain {
        collectionMap.clear()
        try {
            wrapCBLError { error ->
                actual.setJSON(json, error)
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException("Failed parsing JSON", e)
        }
    }

    public actual fun setValue(key: String, value: Any?): MutableDictionary = chain {
        checkSelf(value)
        checkType(value)
        when (value) {
            // Booleans treated as numbers unless explicitly using boolean API
            is Boolean -> setBoolean(value, key)
            else -> setValue(value?.actualIfDelegated(), key)
        }
        if (value is Array || value is Dictionary) {
            collectionMap[key] = value
        } else {
            collectionMap.remove(key)
        }
    }

    public actual fun setString(key: String, value: String?): MutableDictionary = chain {
        setString(value, key)
        collectionMap.remove(key)
    }

    public actual fun setNumber(key: String, value: Number?): MutableDictionary = chain {
        setNumber(value as NSNumber?, key)
        collectionMap.remove(key)
    }

    public actual fun setInt(key: String, value: Int): MutableDictionary = chain {
        setInteger(value.convert(), key)
        collectionMap.remove(key)
    }

    public actual fun setLong(key: String, value: Long): MutableDictionary = chain {
        setLongLong(value, key)
        collectionMap.remove(key)
    }

    public actual fun setFloat(key: String, value: Float): MutableDictionary = chain {
        setFloat(value, key)
        collectionMap.remove(key)
    }

    public actual fun setDouble(key: String, value: Double): MutableDictionary = chain {
        setDouble(value, key)
        collectionMap.remove(key)
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDictionary = chain {
        setBoolean(value, key)
        collectionMap.remove(key)
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDictionary = chain {
        setBlob(value?.actual, key)
        collectionMap.remove(key)
    }

    public actual fun setDate(key: String, value: Instant?): MutableDictionary = chain {
        setDate(value?.toNSDate(), key)
        collectionMap.remove(key)
    }

    public actual fun setArray(key: String, value: Array?): MutableDictionary = chain {
        setArray(value?.actual, key)
        collectionMap.remove(key)
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDictionary = chain {
        checkSelf(value)
        setDictionary(value?.actual, key)
        collectionMap.remove(key)
    }

    public actual fun remove(key: String): MutableDictionary = chain {
        removeValueForKey(key)
        collectionMap.remove(key)
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
        throw IllegalStateException("Mutable objects may not be encoded as JSON")
    }

    // Java performs this check, but Objective-C does not
    private fun checkSelf(value: Any?) {
        if (value === this) {
            throw IllegalArgumentException("Dictionaries cannot ba added to themselves")
        }
    }
}

internal fun CBLMutableDictionary.asMutableDictionary() = MutableDictionary(this)
