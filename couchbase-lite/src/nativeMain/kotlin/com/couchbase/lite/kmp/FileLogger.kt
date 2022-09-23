package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toFLString
import kotlinx.cinterop.memScoped
import libcblite.CBLLog_SetFileConfig
import libcblite.CBL_LogMessage

public actual class FileLogger : Logger {

    public actual var config: LogFileConfiguration? = null
        set(value) {
            value ?: error("Can't set FileLogger.config to null in C SDK")
            field = value
            wrapError { error ->
                CBLLog_SetFileConfig(value.getActual(level), error)
            }
        }

    actual override var level: LogLevel = LogLevel.NONE
        set(value) {
            field = value
            if (config != null) {
                // set actual config with new level
                config = config
            }
        }

    override fun log(level: LogLevel, domain: LogDomain, message: String) {
        memScoped {
            CBL_LogMessage(domain.actual, level.actual, message.toFLString(this))
        }
    }
}
