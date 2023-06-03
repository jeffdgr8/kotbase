package kotbase

import cocoapods.CouchbaseLite.CBLQueryParameters
import kotbase.base.DelegatedClass
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

    public actual fun setString(name: String, value: String?): Parameters {
        actual.setString(value, name)
        return this
    }

    public actual fun setNumber(name: String, value: Number?): Parameters {
        actual.setNumber(value as NSNumber?, name)
        return this
    }

    public actual fun setInt(name: String, value: Int): Parameters {
        actual.setInteger(value.convert(), name)
        return this
    }

    public actual fun setLong(name: String, value: Long): Parameters {
        actual.setLongLong(value, name)
        return this
    }

    public actual fun setFloat(name: String, value: Float): Parameters {
        actual.setFloat(value, name)
        return this
    }

    public actual fun setDouble(name: String, value: Double): Parameters {
        actual.setDouble(value, name)
        return this
    }

    public actual fun setBoolean(name: String, value: Boolean): Parameters {
        actual.setBoolean(value, name)
        return this
    }

    public actual fun setDate(name: String, value: Instant?): Parameters {
        actual.setDate(value?.toNSDate(), name)
        return this
    }

    public actual fun setBlob(name: String, value: Blob?): Parameters {
        actual.setBlob(value?.actual, name)
        return this
    }

    public actual fun setDictionary(name: String, value: Dictionary?): Parameters {
        actual.setDictionary(value?.actual, name)
        return this
    }

    public actual fun setArray(name: String, value: Array?): Parameters {
        actual.setArray(value?.actual, name)
        return this
    }

    public actual fun setValue(name: String, value: Any?): Parameters {
        actual.setValue(value?.actualIfDelegated(), name)
        return this
    }
}

internal fun CBLQueryParameters.asParameters() = Parameters(this)
