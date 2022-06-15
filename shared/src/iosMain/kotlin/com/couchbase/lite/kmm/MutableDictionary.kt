package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLMutableDictionary
import com.couchbase.lite.kmm.ext.throwError
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.chain
import com.udobny.kmm.ext.toNSNumber
import com.udobny.kmm.ext.toNativeDateDeep
import com.udobny.kmm.ext.toNativeDateValuesDeep
import com.udobny.kmm.ext.wrapError
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSError

public actual class MutableDictionary
internal constructor(override val actual: CBLMutableDictionary) : Dictionary(actual) {

    public actual constructor() : this(CBLMutableDictionary())

    public actual constructor(data: Map<String, Any?>) : this(
        @Suppress("UNCHECKED_CAST")
        CBLMutableDictionary((data as Map<Any?, *>).toNativeDateValuesDeep())
    )

    public actual constructor(json: String) : this(
        wrapError(NSError::toCouchbaseLiteException) { error ->
            CBLMutableDictionary(json, error)
        }
    )

    private inline fun chain(action: CBLMutableDictionary.() -> Unit) = chain(actual, action)

    public actual fun setData(data: Map<String, Any?>): MutableDictionary = chain {
        @Suppress("UNCHECKED_CAST")
        setData((data as Map<Any?, *>).toNativeDateValuesDeep())
    }

    public actual fun setJSON(json: String): MutableDictionary = chain {
        throwError { error ->
            setJSON(json, error)
        }
    }

    public actual fun setValue(key: String, value: Any?): MutableDictionary = chain {
        setValue(value?.toNativeDateDeep(), key)
    }

    public actual fun setString(key: String, value: String?): MutableDictionary = chain {
        setString(value, key)
    }

    public actual fun setNumber(key: String, value: Number?): MutableDictionary = chain {
        setNumber(value?.toNSNumber(), key)
    }

    public actual fun setInt(key: String, value: Int): MutableDictionary = chain {
        setInteger(value.toLong(), key)
    }

    public actual fun setLong(key: String, value: Long): MutableDictionary = chain {
        setLongLong(value, key)
    }

    public actual fun setFloat(key: String, value: Float): MutableDictionary = chain {
        setFloat(value, key)
    }

    public actual fun setDouble(key: String, value: Double): MutableDictionary = chain {
        setDouble(value, key)
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDictionary = chain {
        setBoolean(value, key)
    }

    public actual fun setBlob(key: String, value: Blob): MutableDictionary = chain {
        setBlob(value.actual, key)
    }

    public actual fun setDate(key: String, value: Instant): MutableDictionary = chain {
        setDate(value.toNSDate(), key)
    }

    public actual fun setArray(key: String, value: Array): MutableDictionary = chain {
        setArray(value.actual, key)
    }

    public actual fun setDictionary(key: String, value: Dictionary): MutableDictionary = chain {
        setDictionary(value.actual, key)
    }

    public actual fun remove(key: String): MutableDictionary = chain {
        removeValueForKey(key)
    }

    actual override fun getArray(key: String): MutableArray? =
        actual.arrayForKey(key)?.asMutableArray()

    actual override fun getDictionary(key: String): MutableDictionary? =
        actual.dictionaryForKey(key)?.asMutableDictionary()
}

internal fun CBLMutableDictionary.asMutableDictionary() = MutableDictionary(this)
