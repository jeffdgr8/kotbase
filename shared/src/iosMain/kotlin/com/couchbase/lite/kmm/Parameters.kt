package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLQueryParameters
import com.udobny.kmm.DelegatedClass
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSNumber

public actual class Parameters
internal constructor(actual: CBLQueryParameters) :
    DelegatedClass<CBLQueryParameters>(actual) {

    public actual constructor(parameters: Parameters?) : this(
        CBLQueryParameters(parameters?.actual)
    )

    public actual fun getValue(name: String): Any? =
        actual.valueForName(name)?.delegateIfNecessary()

    public actual fun setString(name: String, value: String?): Parameters = chain {
        setString(value, name)
    }

    public actual fun setNumber(name: String, value: Number?): Parameters = chain {
        setNumber(value as NSNumber?, name)
    }

    public actual fun setInt(name: String, value: Int): Parameters = chain {
        setInteger(value.convert(), name)
    }

    public actual fun setLong(name: String, value: Long): Parameters = chain {
        setLongLong(value, name)
    }

    public actual fun setFloat(name: String, value: Float): Parameters = chain {
        setFloat(value, name)
    }

    public actual fun setDouble(name: String, value: Double): Parameters = chain {
        setDouble(value, name)
    }

    public actual fun setBoolean(name: String, value: Boolean): Parameters = chain {
        setBoolean(value, name)
    }

    public actual fun setDate(name: String, value: Instant?): Parameters = chain {
        setDate(value?.toNSDate(), name)
    }

    public actual fun setBlob(name: String, value: Blob?): Parameters = chain {
        setBlob(value?.actual, name)
    }

    public actual fun setDictionary(name: String, value: Dictionary?): Parameters = chain {
        setDictionary(value?.actual, name)
    }

    public actual fun setArray(name: String, value: Array?): Parameters = chain {
        setArray(value?.actual, name)
    }

    public actual fun setValue(name: String, value: Any?): Parameters = chain {
        setValue(value?.actualIfDelegated(), name)
    }
}

internal fun CBLQueryParameters.asParameters() = Parameters(this)
