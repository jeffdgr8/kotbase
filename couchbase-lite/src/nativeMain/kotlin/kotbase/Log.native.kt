package kotbase

import kotbase.internal.fleece.toKString
import kotlinx.cinterop.staticCFunction
import libcblite.CBLLog_SetCallback
import libcblite.CBLLog_SetCallbackLevel
import libcblite.kCBLLogNone

public actual class Log internal constructor() {

    public actual val console: ConsoleLogger by lazy {
        ConsoleLogger()
    }

    public actual val file: FileLogger by lazy {
        FileLogger()
    }

    public actual var custom: Logger?
        get() = topLevelCustom
        set(value) {
            topLevelCustom = value
            if (value != null) {
                CBLLog_SetCallbackLevel(value.level.actual)
                CBLLog_SetCallback(
                    staticCFunction { domain, level, message ->
                        topLevelCustom?.log(
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

private var topLevelCustom: Logger? = null
