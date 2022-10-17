package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.wrapCBLError
import kotlinx.cinterop.memScoped
import libcblite.CBLLog_SetFileConfig
import libcblite.CBL_LogMessage
import okio.FileSystem
import okio.Path.Companion.toPath

public actual class FileLogger internal constructor() : Logger {

    public actual var config: LogFileConfiguration? = null
        set(value) {
            field = value
            if (value != null) {
                FileSystem.SYSTEM.createDirectories(value.directory.toPath(), false)
            }
            val actual = value?.getActual(level) ?: LogFileConfiguration.getNullActual()
            wrapCBLError { error ->
                CBLLog_SetFileConfig(actual, error)
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
