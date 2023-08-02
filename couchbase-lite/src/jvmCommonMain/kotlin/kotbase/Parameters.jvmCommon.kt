package kotbase

import kotbase.base.DelegatedClass
import kotbase.ext.toDate
import kotlinx.datetime.Instant
import com.couchbase.lite.Parameters as CBLParameters

public actual class Parameters
internal constructor(actual: CBLParameters) : DelegatedClass<CBLParameters>(actual) {

    public actual constructor(parameters: Parameters?) : this(CBLParameters(parameters?.actual))

    public actual fun getValue(name: String): Any? =
        actual.getValue(name)?.delegateIfNecessary()

    public actual fun setString(name: String, value: String?): Parameters {
        actual.setString(name, value)
        return this
    }

    public actual fun setNumber(name: String, value: Number?): Parameters {
        actual.setNumber(name, value)
        return this
    }

    public actual fun setInt(name: String, value: Int): Parameters {
        actual.setInt(name, value)
        return this
    }

    public actual fun setLong(name: String, value: Long): Parameters {
        actual.setLong(name, value)
        return this
    }

    public actual fun setFloat(name: String, value: Float): Parameters {
        actual.setFloat(name, value)
        return this
    }

    public actual fun setDouble(name: String, value: Double): Parameters {
        actual.setDouble(name, value)
        return this
    }

    public actual fun setBoolean(name: String, value: Boolean): Parameters {
        actual.setBoolean(name, value)
        return this
    }

    public actual fun setDate(name: String, value: Instant?): Parameters {
        actual.setDate(name, value?.toDate())
        return this
    }

    public actual fun setBlob(name: String, value: Blob?): Parameters {
        actual.setBlob(name, value?.actual)
        return this
    }

    public actual fun setDictionary(name: String, value: Dictionary?): Parameters {
        actual.setDictionary(name, value?.actual)
        return this
    }

    public actual fun setArray(name: String, value: Array?): Parameters {
        actual.setArray(name, value?.actual)
        return this
    }

    public actual fun setValue(name: String, value: Any?): Parameters {
        actual.setValue(name, value?.actualIfDelegated())
        return this
    }
}

internal fun CBLParameters.asParameters() = Parameters(this)
