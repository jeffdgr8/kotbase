/*
 * Copyright 2025 Jeff Lockhart
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
package kotbase.logging

import kotbase.LogDomain
import kotbase.LogLevel

/**
 * A log sink that writes log messages to a custom log sink implementation.
 *
 * @constructor Initializes a `CustomLogSink` with the specified log level, log domains,
 * and custom log sink implementation. The default log domain is set to all domains.
 */
public class CustomLogSink(internal val level: LogLevel, vararg domains: LogDomain, internal val logSink: LogSink) {
    internal val domains = domains.toSet().ifEmpty { LogDomain.ALL }
}

/**
 * Functional interface for custom log sinks to handle log messages.
 */
public fun interface LogSink {

    public fun writeLog(level: LogLevel, domain: LogDomain, message: String)
}
