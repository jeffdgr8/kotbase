package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLFileLogger
import com.udobny.kmm.DelegatedClass

public actual class FileLogger
internal constructor(override val actual: CBLFileLogger) :
    DelegatedClass<CBLFileLogger>(actual), Logger {

    public actual var config: LogFileConfiguration?
        get() = actual.config?.asLogFileConfiguration()
        set(value) {
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
