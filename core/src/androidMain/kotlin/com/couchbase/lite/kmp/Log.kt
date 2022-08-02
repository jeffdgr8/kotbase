package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class Log
internal constructor(actual: com.couchbase.lite.Log) :
    DelegatedClass<com.couchbase.lite.Log>(actual) {

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
