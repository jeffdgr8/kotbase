package kotbase

import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.memScoped
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import libcblite.CBLLog_SetFileConfig
import libcblite.CBL_LogMessage

public actual class FileLogger internal constructor() : Logger {

    public actual var config: LogFileConfiguration? = null
        set(value) {
            field = value
            if (value != null) {
                SystemFileSystem.createDirectories(Path(value.directory), false)
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

    actual override fun log(level: LogLevel, domain: LogDomain, message: String) {
        memScoped {
            CBL_LogMessage(domain.actual, level.actual, message.toFLString(this))
        }
    }
}
