package kotbase

import com.couchbase.lite.FileLogger
import kotbase.base.DelegatedClass

public actual class FileLogger
internal constructor(override val actual: com.couchbase.lite.FileLogger) :
    DelegatedClass<FileLogger>(actual), Logger {

    public actual var config: LogFileConfiguration?
        get() = actual.config?.asLogFileConfiguration()
        set(value) {
            actual.config = value?.actual
        }

    actual override var level: LogLevel
        get() = actual.level
        set(value) {
            actual.level = value
        }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        actual.log(level, domain.actual, message)
    }
}
