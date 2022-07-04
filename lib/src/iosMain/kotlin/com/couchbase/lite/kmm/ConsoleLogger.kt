package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLConsoleLogger
import com.udobny.kmm.DelegatedClass

public actual class ConsoleLogger internal constructor(override val actual: CBLConsoleLogger) :
    DelegatedClass<CBLConsoleLogger>(actual), Logger {

    public actual var domains: Set<LogDomain>
        get() = actual.domains.toLogDomain()
        set(value) {
            actual.domains = value.toCBLLogDomain()
        }

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
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
