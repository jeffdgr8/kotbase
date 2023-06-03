package kotbase

import com.couchbase.lite.Log
import kotbase.base.DelegatedClass

public actual class Log
internal constructor(actual: com.couchbase.lite.Log) :
    DelegatedClass<Log>(actual) {

    public actual val console: ConsoleLogger = ConsoleLogger(actual.console)
        get() {
            // access the underlying logger on each get(), required to satisfy PreInitTest
            actual.console
            return field
        }

    public actual val file: FileLogger = FileLogger(actual.file)
        get() {
            // access the underlying logger on each get(), required to satisfy PreInitTest
            actual.file
            return field
        }

    public actual var custom: Logger? = null
        set(value) {
            field = value
            actual.custom = value?.convert()
        }
}
