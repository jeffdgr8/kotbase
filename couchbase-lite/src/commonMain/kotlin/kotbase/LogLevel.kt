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
 * Log level.
 */
public expect enum class LogLevel {

    /**
     * Debugging information.
     */
    DEBUG,

    /**
     * Low level logging.
     */
    VERBOSE,

    /**
     * Essential state info and client errors that are recoverable
     */
    INFO,

    /**
     * Internal errors that are recoverable; client errors that may not be recoverable
     */
    WARNING,

    /**
     * Internal errors that are unrecoverable
     */
    ERROR,

    /**
     * Disabling log messages of a given log domain.
     */
    NONE
}
