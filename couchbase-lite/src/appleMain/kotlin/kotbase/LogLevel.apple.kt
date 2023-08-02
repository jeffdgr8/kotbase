package kotbase

import cocoapods.CouchbaseLite.CBLLogLevel
import cocoapods.CouchbaseLite.CBLLogLevel.*

public actual enum class LogLevel {
    DEBUG,
    VERBOSE,
    INFO,
    WARNING,
    ERROR,
    NONE;

    public val actual: CBLLogLevel
        get() = when (this) {
            DEBUG -> kCBLLogLevelDebug
            VERBOSE -> kCBLLogLevelVerbose
            INFO -> kCBLLogLevelInfo
            WARNING -> kCBLLogLevelWarning
            ERROR -> kCBLLogLevelError
            NONE -> kCBLLogLevelNone
        }

    internal companion object {

        internal fun from(logLevel: CBLLogLevel): LogLevel = when (logLevel) {
            kCBLLogLevelDebug -> DEBUG
            kCBLLogLevelVerbose -> VERBOSE
            kCBLLogLevelInfo -> INFO
            kCBLLogLevelWarning -> WARNING
            kCBLLogLevelError -> ERROR
            kCBLLogLevelNone -> NONE
            else -> error("Unexpected CBLLogLevel ($logLevel)")
        }
    }
}
