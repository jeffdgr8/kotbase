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

import kotbase.internal.DelegatedClass
import java.util.*
import com.couchbase.lite.ConsoleLogger as CBLConsoleLogger
import com.couchbase.lite.LogDomain as CBLLogDomain

@Deprecated("Use ConsoleLogSink")
public actual class ConsoleLogger
internal constructor(override val actual: CBLConsoleLogger) : DelegatedClass<CBLConsoleLogger>(actual), Logger {

    public actual var domains: Set<LogDomain>
        get() = actual.domains.map { LogDomain.from(it) }.toSet()
        set(value) {
            actual.domains = if (value.isEmpty()) {
                EnumSet.noneOf(CBLLogDomain::class.java)
            } else {
                EnumSet.copyOf(value.map { it.actual })
            }
        }

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
    }

    actual override var level: LogLevel
        get() = actual.level
        set(value) {
            actual.level = value
        }

    actual override fun log(level: LogLevel, domain: LogDomain, message: String) {
        actual.log(level, domain.actual, message)
    }
}
