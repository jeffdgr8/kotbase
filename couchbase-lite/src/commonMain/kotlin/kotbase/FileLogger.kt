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
 * A logger for writing to a file in the application's storage so
 * that log messages can persist durably after the application has
 * stopped or encountered a problem.  Each log level is written to
 * a separate file.
 */
@Suppress("DEPRECATION")
@Deprecated("Use FileLogSink")
public expect class FileLogger : Logger {

    /**
     * The lowest level that will be logged to the logging files.
     */
    override var level: LogLevel

    override fun log(level: LogLevel, domain: LogDomain, message: String)

    /**
     * Gets the configuration currently in use by the file logger.
     * Note the configuration returned from this method is read-only
     * and cannot be modified. An attempt to modify it will throw an exception.
     */
    public var config: LogFileConfiguration?
}
