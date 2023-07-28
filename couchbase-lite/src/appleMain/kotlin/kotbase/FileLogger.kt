package kotbase

import cocoapods.CouchbaseLite.CBLFileLogger
import kotbase.base.DelegatedClass

public actual class FileLogger
internal constructor(override val actual: CBLFileLogger) : DelegatedClass<CBLFileLogger>(actual), Logger {

    public actual var config: LogFileConfiguration? = null
        get() = field ?: actual.config?.asReadOnlyLogFileConfiguration()
        set(value) {
            field = value
            value?.readonly = true
            actual.config = value?.actual
        }

    actual override var level: LogLevel
        get() = LogLevel.from(actual.level)
        set(value) {
            actual.setLevel(value.actual)
        }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        actual.logWithLevel(level.actual, domain.actual, message)
    }
}
