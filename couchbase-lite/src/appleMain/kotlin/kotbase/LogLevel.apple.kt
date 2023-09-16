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

import cocoapods.CouchbaseLite.CBLLogLevel
import cocoapods.CouchbaseLite.CBLLogLevel.*

public actual enum class LogLevel {
    DEBUG,
    VERBOSE,
    INFO,
    WARNING,
    ERROR,
    NONE;

    internal val actual: CBLLogLevel
        get() = when (this) {
            DEBUG -> kCBLLogLevelDebug
            VERBOSE -> kCBLLogLevelVerbose
            INFO -> kCBLLogLevelInfo
            WARNING -> kCBLLogLevelWarning
            ERROR -> kCBLLogLevelError
            NONE -> kCBLLogLevelNone
        }

    internal companion object {

        internal fun from(logLevel: CBLLogLevel): LogLevel = when (logLevel) {
            kCBLLogLevelDebug -> DEBUG
            kCBLLogLevelVerbose -> VERBOSE
            kCBLLogLevelInfo -> INFO
            kCBLLogLevelWarning -> WARNING
            kCBLLogLevelError -> ERROR
            kCBLLogLevelNone -> NONE
            else -> error("Unexpected CBLLogLevel ($logLevel)")
        }
    }
}
