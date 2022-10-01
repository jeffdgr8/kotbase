package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.*
import com.couchbase.lite.kmp.internal.fleece.parseJson
import com.couchbase.lite.kmp.internal.fleece.setString
import com.couchbase.lite.kmp.internal.fleece.setValue
import kotlinx.datetime.Instant
import libcblite.*

public actual class MutableDictionary
internal constructor(override val actual: FLMutableDict) : Dictionary(actual) {

    public actual constructor() : this(FLMutableDict_New()!!) {
        // TODO: make sure this is called after it's retained for the second time
        FLMutableDict_Release(actual)
    }

    public actual constructor(data: Map<String, Any?>) : this() {
        setData(data)
    }

    public actual constructor(json: String) : this(
        // TODO: fix double retain
        wrapFLError { error ->
            FLMutableDict_NewFromJSON(json.toFLString(), error)!!
        }
    )

    override val isMutable: Boolean = true

    public actual fun setData(data: Map<String, Any?>): MutableDictionary {
        FLMutableDict_RemoveAll(actual)
        data.forEach { (key, value) ->
            setValue(key, value)
        }
        return this
    }

    public actual fun setJSON(json: String): MutableDictionary {
        @Suppress("UNCHECKED_CAST")
        val data = parseJson(json) as? Map<String, Any?>
            ?: error("Parsed result is not a Dictionary")
        setData(data)
        return this
    }

    public actual fun setValue(key: String, value: Any?): MutableDictionary {
        actual.setValue(key, value)
        return this
    }

    public actual fun setString(key: String, value: String?): MutableDictionary {
        actual.setString(key, value)
        return this
    }

    public actual fun setNumber(key: String, value: Number?): MutableDictionary {
        actual.setNumber(key, value)
        return this
    }

    public actual fun setInt(key: String, value: Int): MutableDictionary {
        actual.setInt(key, value)
        return this
    }

    public actual fun setLong(key: String, value: Long): MutableDictionary {
        actual.setLong(key, value)
        return this
    }

    public actual fun setFloat(key: String, value: Float): MutableDictionary {
        actual.setFloat(key, value)
        return this
    }

    public actual fun setDouble(key: String, value: Double): MutableDictionary {
        actual.setDouble(key, value)
        return this
    }

    public actual fun setBoolean(key: String, value: Boolean): MutableDictionary {
        actual.setBoolean(key, value)
        return this
    }

    public actual fun setBlob(key: String, value: Blob?): MutableDictionary {
        actual.setBlob(key, value)
        return this
    }

    public actual fun setDate(key: String, value: Instant?): MutableDictionary {
        actual.setDate(key, value)
        return this
    }

    public actual fun setArray(key: String, value: Array?): MutableDictionary {
        actual.setArray(key, value)
        return this
    }

    public actual fun setDictionary(key: String, value: Dictionary?): MutableDictionary {
        actual.setDictionary(key, value)
        return this
    }

    public actual fun remove(key: String): MutableDictionary {
        actual.remove(key)
        return this
    }

    actual override fun getArray(key: String): MutableArray? =
        super.getArray(key) as MutableArray?

    actual override fun getDictionary(key: String): MutableDictionary? =
        super.getDictionary(key) as MutableDictionary?
}

internal fun FLMutableDict.asMutableDictionary() = MutableDictionary(this)
