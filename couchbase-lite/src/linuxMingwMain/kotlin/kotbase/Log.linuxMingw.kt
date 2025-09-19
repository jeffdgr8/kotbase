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
@file:Suppress("DEPRECATION")

package kotbase

import kotbase.internal.fleece.toKString
import kotlinx.cinterop.staticCFunction
import libcblite.CBLLog_SetCallback
import libcblite.CBLLog_SetCallbackLevel
import libcblite.kCBLLogNone

@Deprecated("Use LogSinks")
public actual class Log internal constructor() {

    @Deprecated("Use LogSinks.console")
    public actual val console: ConsoleLogger by lazy {
        ConsoleLogger()
    }

    @Deprecated("Use LogSinks.file")
    public actual val file: FileLogger by lazy {
        FileLogger()
    }

    @Deprecated("Use LogSinks.custom")
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
