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
 * Holder for the three Couchbase Lite loggers: console, file, and custom.
 */
@Deprecated("Use LogSinks")
public expect class Log {

    /**
     * The logger that writes to the system console
     */
    @Deprecated("Use LogSinks.console")
    public val console: ConsoleLogger

    /**
     * The logger that writes to log files
     */
    @Deprecated("Use LogSinks.file")
    public val file: FileLogger

    /**
     * An application specific logging method
     */
    @Deprecated("Use LogSinks.custom")
    public var custom: Logger?
}
