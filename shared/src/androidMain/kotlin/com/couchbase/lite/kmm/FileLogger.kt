package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class FileLogger
internal constructor(override val actual: com.couchbase.lite.FileLogger) :
    DelegatedClass<com.couchbase.lite.FileLogger>(actual), Logger {

    public actual var config: LogFileConfiguration?
        get() = actual.config
        set(value) {
            actual.config = value
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
