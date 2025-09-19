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

/**
 * A class that sends log messages to the system log, available via 'logcat' on Android.
 */
@Suppress("DEPRECATION")
@Deprecated("Use ConsoleLogSink")
public expect class ConsoleLogger : Logger {

    /**
     * The lowest level that will be logged to the console.
     */
    override var level: LogLevel

    override fun log(level: LogLevel, domain: LogDomain, message: String)

    /**
     * The set of domains currently being logged to the console.
     */
    public var domains: Set<LogDomain>

    /**
     * Sets the domains that will be considered for writing to the console log.
     *
     * @param domains The domains to make active (vararg)
     */
    public fun setDomains(vararg domains: LogDomain)
}
