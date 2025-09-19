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

import cocoapods.CouchbaseLite.CBLConsoleLogger
import kotbase.internal.DelegatedClass

@Suppress("DEPRECATION")
@Deprecated("Use ConsoleLogSink")
public actual class ConsoleLogger
internal constructor(override val actual: CBLConsoleLogger) : DelegatedClass<CBLConsoleLogger>(actual), Logger {

    public actual var domains: Set<LogDomain>
        get() = actual.domains.toLogDomain()
        set(value) {
            actual.domains = value.toCBLLogDomain()
        }

    public actual fun setDomains(vararg domains: LogDomain) {
        this.domains = domains.toSet()
    }

    actual override var level: LogLevel
        get() = LogLevel.from(actual.level)
        set(value) {
            actual.setLevel(value.actual)
        }

    actual override fun log(level: LogLevel, domain: LogDomain, message: String) {
        actual.logWithLevel(level.actual, domain.actual, message)
    }
}
