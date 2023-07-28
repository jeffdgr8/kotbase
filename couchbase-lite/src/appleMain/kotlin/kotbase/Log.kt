package kotbase

import cocoapods.CouchbaseLite.CBLLog
import kotbase.base.DelegatedClass

public actual class Log
internal constructor(actual: CBLLog) : DelegatedClass<CBLLog>(actual) {

    public actual val console: ConsoleLogger by lazy {
        ConsoleLogger(actual.console)
    }

    public actual val file: FileLogger by lazy {
        FileLogger(actual.file)
    }

    public actual var custom: Logger? = null
        set(value) {
            field = value
            actual.custom = value?.convert()
        }
}
