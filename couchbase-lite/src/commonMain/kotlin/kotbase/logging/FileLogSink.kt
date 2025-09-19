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

import kotbase.LogLevel
import kotbase.Defaults

/**
 * A log sink that writes log messages to files.
 */
public class FileLogSink(

    /**
     * The minimum log level of the log messages to be logged.
     */
    public val level: LogLevel = LogLevel.WARNING,

    /**
     * The directory where the log files will be stored.
     */
    public val directory: String,

    /**
     * To use plain text file format instead of the default binary format.
     * The default is [Defaults.FileLogSink.USE_PLAINTEXT].
     */
    public val isPlainText: Boolean = Defaults.FileLogSink.USE_PLAINTEXT,

    /**
     * The max number of rotated log files to keep.
     * The default is [Defaults.FileLogSink.MAX_KEPT_FILES].
     */
    public val maxKeptFiles: Int = Defaults.FileLogSink.MAX_KEPT_FILES,

    /**
     * The maximum size of a log file before being rotated in bytes.
     * The default value is [Defaults.FileLogSink.MAX_SIZE].
     */
    public val maxFileSize: Long = Defaults.FileLogSink.MAX_SIZE
)
