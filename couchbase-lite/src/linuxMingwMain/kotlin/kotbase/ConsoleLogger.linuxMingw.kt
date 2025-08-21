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

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import libcblite.CBLLog_ConsoleLevel
import libcblite.CBLLog_SetConsoleLevel
import platform.posix.pthread_self

@Suppress("DEPRECATION")
@Deprecated("Use ConsoleLogSink")
public actual class ConsoleLogger internal constructor() : Logger {

    public actual var domains: Set<LogDomain> = LogDomain.ALL

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
    }

    actual override var level: LogLevel
        get() = LogLevel.from(CBLLog_ConsoleLevel())
        set(value) {
            CBLLog_SetConsoleLevel(value.actual)
        }

    actual override fun log(level: LogLevel, domain: LogDomain, message: String) {
        if (level < this.level || !domains.contains(domain)) return
        println(formatLog(level, domain.name, message))
    }

    private fun formatLog(level: LogLevel, domain: String, message: String): String {
        val thread = pthread_self().toString().padStart(THREAD_FIELD_LEN, ' ')
        val time = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toString()
        return "$time$thread $level$LOG_TAG$domain: $message"
    }

    private companion object {
        private const val LOG_TAG = "/CouchbaseLite/"
        private const val THREAD_FIELD_LEN = 7
    }
}
