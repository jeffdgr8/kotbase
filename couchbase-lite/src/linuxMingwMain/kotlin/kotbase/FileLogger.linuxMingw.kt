/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import kotbase.internal.fleece.toFLString
import kotbase.internal.wrapCBLError
import kotlinx.cinterop.memScoped
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import libcblite.CBLLog_SetFileConfig
import libcblite.CBL_LogMessage

@Suppress("DEPRECATION")
@Deprecated("Use FileLogSink")
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
