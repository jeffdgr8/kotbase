package kotbase

import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import libcblite.CBLLogFileConfiguration
import platform.posix.strdup
import platform.posix.strlen

public actual class LogFileConfiguration
public actual constructor(public actual val directory: String) {

    public actual constructor(config: LogFileConfiguration) : this(config.directory, config)

    public actual constructor(directory: String, config: LogFileConfiguration?) : this(directory) {
        config?.let {
            maxRotateCount = it.maxRotateCount
            maxSize = it.maxSize
            usesPlaintext = it.usesPlaintext
        }
    }

    private var readonly: Boolean = false

    public actual fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration {
        checkReadOnly()
        usesPlaintext = usePlaintext
        return this
    }

    public actual var maxRotateCount: Int = 1
        set(value) {
            checkReadOnly()
            field = value
        }

    public actual fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration {
        checkReadOnly()
        this.maxRotateCount = maxRotateCount
        return this
    }

    public actual var maxSize: Long = 0
        set(value) {
            checkReadOnly()
            field = value
        }

    public actual fun setMaxSize(maxSize: Long): LogFileConfiguration {
        checkReadOnly()
        this.maxSize = maxSize
        return this
    }

    public actual var usesPlaintext: Boolean = false
        set(value) {
            checkReadOnly()
            field = value
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is LogFileConfiguration) {
            return false
        }
        return maxRotateCount == other.maxRotateCount
                && directory == other.directory
                && maxSize == other.maxSize
                && usesPlaintext == other.usesPlaintext
    }

    override fun hashCode(): Int = directory.hashCode()

    internal fun getActual(level: LogLevel): CValue<CBLLogFileConfiguration> {
        readonly = true
        return cValue {
            with(directory) {
                buf = strdup(this@LogFileConfiguration.directory)
                size = strlen(this@LogFileConfiguration.directory)
            }
            this.level = level.actual
            maxRotateCount = this@LogFileConfiguration.maxRotateCount.convert()
            maxSize = this@LogFileConfiguration.maxSize.convert()
            usePlaintext = this@LogFileConfiguration.usesPlaintext
        }
    }

    private fun checkReadOnly() {
        if (readonly) throw IllegalStateException("LogFileConfiguration is readonly mode.")
    }

    internal companion object {
        internal fun getNullActual(): CValue<CBLLogFileConfiguration> = cValue()
    }
}
