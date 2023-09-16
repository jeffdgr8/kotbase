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
 * Threading policy: This class is certain to be used from multiple
 * threads.  As long as it is thread safe, the various race conditions
 * are unlikely and the penalties very small.  "Volatile" ensures
 * the thread safety and the several races are tolerable.
 */
public expect class FileLogger : Logger {

    /**
     * The maximum logging level that will be written to the logging files.
     */
    override var level: LogLevel

    override fun log(level: LogLevel, domain: LogDomain, message: String)

    /**
     * The configuration currently in use by the file logger.
     * Note that once a configuration has been installed in a logger,
     * the configuration is read-only and can no longer be modified.
     * An attempt to modify the configuration returned by this method will cause an exception.
     */
    public var config: LogFileConfiguration?
}
