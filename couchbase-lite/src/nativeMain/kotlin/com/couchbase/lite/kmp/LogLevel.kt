package com.couchbase.lite.kmp

import kotlinx.cinterop.convert
import libcblite.*

public actual enum class LogLevel {
    DEBUG,
    VERBOSE,
    INFO,
    WARNING,
    ERROR,
    NONE;

    internal val actual: CBLLogLevel
        get() = when (this) {
            DEBUG -> kCBLLogDebug
            VERBOSE -> kCBLLogVerbose
            INFO -> kCBLLogInfo
            WARNING -> kCBLLogWarning
            ERROR -> kCBLLogError
            NONE -> kCBLLogNone
        }.convert()

    internal companion object {

        internal fun from(logLevel: CBLLogLevel): LogLevel {
            return when (logLevel.toUInt()) {
                kCBLLogDebug -> DEBUG
                kCBLLogVerbose -> VERBOSE
                kCBLLogInfo -> INFO
                kCBLLogWarning -> WARNING
                kCBLLogError -> ERROR
                kCBLLogNone -> NONE
                else -> error("Unexpected CBLLogLevel")
            }
        }
    }
}