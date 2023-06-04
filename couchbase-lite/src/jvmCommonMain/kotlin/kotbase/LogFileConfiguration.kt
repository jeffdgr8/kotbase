package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.LogFileConfiguration as CBLLogFileConfiguration

public actual class LogFileConfiguration
internal constructor(actual: CBLLogFileConfiguration) : DelegatedClass<CBLLogFileConfiguration>(actual) {

    public actual constructor(directory: String) : this(CBLLogFileConfiguration(directory))

    public actual constructor(config: LogFileConfiguration) : this(CBLLogFileConfiguration(config.actual))

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(
        CBLLogFileConfiguration(directory, config?.actual)
    )

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration {
        actual.setUsePlaintext(usePlaintext)
        return this
    }

    public actual var maxRotateCount: Int
        get() = actual.maxRotateCount
        set(value) {
            actual.maxRotateCount = value
        }

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration {
        actual.maxRotateCount = maxRotateCount
        return this
    }

    public actual var maxSize: Long
        get() = actual.maxSize
        set(value) {
            actual.maxSize = value
        }

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration {
        actual.maxSize = maxSize
        return this
    }

    public actual var usesPlaintext: Boolean
        get() = actual.usesPlaintext()
        set(value) {
            actual.setUsePlaintext(value)
        }

    public actual val directory: String
        get() = actual.directory
}

internal fun CBLLogFileConfiguration.asLogFileConfiguration() =
    LogFileConfiguration(this)
