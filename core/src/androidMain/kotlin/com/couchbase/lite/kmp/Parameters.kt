package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.ext.toDate
import kotlinx.datetime.Instant

public actual class Parameters
internal constructor(actual: com.couchbase.lite.Parameters) :
    DelegatedClass<com.couchbase.lite.Parameters>(actual) {

    public actual constructor(parameters: Parameters?) : this(
        com.couchbase.lite.Parameters(parameters?.actual)
    )

    public actual fun getValue(name: String): Any? =
        actual.getValue(name)?.delegateIfNecessary()

    public actual fun setString(name: String, value: String?): Parameters = chain {
        setString(name, value)
    }

    public actual fun setNumber(name: String, value: Number?): Parameters = chain {
        setNumber(name, value)
    }

    public actual fun setInt(name: String, value: Int): Parameters = chain {
        setInt(name, value)
    }

    public actual fun setLong(name: String, value: Long): Parameters = chain {
        setLong(name, value)
    }

    public actual fun setFloat(name: String, value: Float): Parameters = chain {
        setFloat(name, value)
    }

    public actual fun setDouble(name: String, value: Double): Parameters = chain {
        setDouble(name, value)
    }

    public actual fun setBoolean(name: String, value: Boolean): Parameters = chain {
        setBoolean(name, value)
    }

    public actual fun setDate(name: String, value: Instant?): Parameters = chain {
        setDate(name, value?.toDate())
    }

    public actual fun setBlob(name: String, value: Blob?): Parameters = chain {
        setBlob(name, value?.actual)
    }

    public actual fun setDictionary(name: String, value: Dictionary?): Parameters = chain {
        setDictionary(name, value?.actual)
    }

    public actual fun setArray(name: String, value: Array?): Parameters = chain {
        setArray(name, value?.actual)
    }

    public actual fun setValue(name: String, value: Any?): Parameters = chain {
        setValue(name, value?.actualIfDelegated())
    }
}

internal fun com.couchbase.lite.Parameters.asParameters() = Parameters(this)
