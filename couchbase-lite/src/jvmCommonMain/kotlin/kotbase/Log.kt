package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Log as CBLLog

public actual class Log
internal constructor(actual: CBLLog) : DelegatedClass<CBLLog>(actual) {

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
