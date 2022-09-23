package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toKString
import kotlinx.cinterop.staticCFunction
import libcblite.*

public actual class Log {

    public actual val console: ConsoleLogger by lazy {
        ConsoleLogger()
    }

    public actual val file: FileLogger by lazy {
        FileLogger()
    }

    public actual var custom: Logger? = null
        set(value) {
            field = value
            if (value != null) {
                CBLLog_SetCallbackLevel(value.level.actual)
                CBLLog_SetCallback(
                    staticCFunction { domain, level, message ->
                        custom?.log(
                            LogLevel.from(level), LogDomain.from(domain), message.toKString()!!
                        )
                    }
                )
            } else {
                CBLLog_SetCallbackLevel(kCBLLogNone.toUByte())
                CBLLog_SetCallback(null)
            }
        }
}
