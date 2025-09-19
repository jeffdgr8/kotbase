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

/**
 * A class that describes the file configuration for the [FileLogger] class.
 * Once a configuration has been assigned to a Logger, it becomes read-only:
 * an attempt to mutate it will cause an exception.
 * To change the configuration of a logger, copy its configuration, mutate the
 * copy and then use it to replace the loggers current configuration.
 */
@Deprecated("Use FileLogSink")
public expect class LogFileConfiguration {

    /**
     * Constructs a file configuration object with the given directory
     *
     * @param directory The directory that the logs will be written to
     */
    public constructor(directory: String)

    /**
     * Constructs a file configuration object based on another one so
     * that it may be modified
     *
     * @param config The other configuration to copy settings from
     */
    public constructor(config: LogFileConfiguration)

    /**
     * Constructs a file configuration object based on another one but changing
     * the directory
     *
     * @param directory The directory that the logs will be written to
     * @param config    The other configuration to copy settings from
     */
    public constructor(directory: String, config: LogFileConfiguration?)

    /**
     * Sets the max size of the log file in bytes. If a log file
     * passes this size then a new log file will be started. This
     * number is a best effort and the actual size may go over slightly.
     * The default size is 500Kb.
     *
     * @param maxSize The max size of the log file in bytes
     * @return The self object
     */
    public fun setMaxSize(maxSize: Long): LogFileConfiguration

    /**
     * Sets the number of rotated logs that are saved. For instance,
     * if the value is 1 then 2 logs will be present: the 'current' log
     * and the previous 'rotated' log.
     * The default value is 1.
     *
     * @param maxRotateCount The number of rotated logs to be saved
     * @return The self object
     */
    public fun setMaxRotateCount(maxRotateCount: Int): LogFileConfiguration

    /**
     * Sets whether or not CBL logs in plaintext. The default (false) is
     * to log in a binary encoded format that is more CPU and I/O friendly.
     * Enabling plaintext is not recommended in production.
     *
     * @param usePlaintext Whether or not to log in plaintext
     * @return The self object
     */
    public fun setUsePlaintext(usePlaintext: Boolean): LogFileConfiguration

    /**
     * The max size of the log file in bytes. If a log file
     * passes this size then a new log file will be started. This
     * number is a best effort and the actual size may go over slightly.
     * The default size is 500Kb.
     */
    public var maxSize: Long

    /**
     * The number of rotated logs that are saved. For instance,
     * if the value is 1 then 2 logs will be present: the 'current' log
     * and the previous 'rotated' log.
     * The default value is 1.
     */
    public var maxRotateCount: Int

    /**
     * Whether or not CBL is logging in plaintext. The default (false) is
     * to log in a binary encoded format that is more CPU and I/O friendly.
     * Enabling plaintext is not recommended in production.
     */
    public var usesPlaintext: Boolean

    /**
     * The directory that the logs files are stored in.
     */
    public val directory: String
}
