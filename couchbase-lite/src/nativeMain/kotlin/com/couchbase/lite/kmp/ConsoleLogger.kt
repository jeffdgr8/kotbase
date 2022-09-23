package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toFLString
import kotlinx.cinterop.memScoped
import libcblite.CBLLog_ConsoleLevel
import libcblite.CBLLog_SetConsoleLevel
import libcblite.CBL_LogMessage

public actual class ConsoleLogger : Logger {

    public actual var domains: Set<LogDomain>
        get() = LogDomain.ALL_DOMAINS
        set(_) {
            // no-op for native, no API support
            println("Couchbase Lite C SDK does not support setting log domains")
        }

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
    }

    actual override var level: LogLevel
        get() = LogLevel.from(CBLLog_ConsoleLevel())
        set(value) {
            CBLLog_SetConsoleLevel(value.actual)
        }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        memScoped {
            CBL_LogMessage(domain.actual, level.actual, message.toFLString(this))
        }
    }
}
