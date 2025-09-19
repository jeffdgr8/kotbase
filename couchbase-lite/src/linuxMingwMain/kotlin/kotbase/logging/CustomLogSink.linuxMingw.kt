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
import kotbase.internal.fleece.toKString
import kotbase.toCBLLogDomain
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.staticCFunction
import libcblite.CBLCustomLogSink
import libcblite.kCBLLogNone

private var topLevelLogSink: LogSink? = null

internal val CustomLogSink?.actual: CValue<CBLCustomLogSink>
    get() {
        topLevelLogSink = this?.logSink
        return cValue {
            level = this@actual?.level?.actual ?: kCBLLogNone.convert()
            domains = this@actual?.domains?.toCBLLogDomain() ?: 0U
            callback = if (this@actual == null) {
                null
            } else {
                staticCFunction { domain, level, message ->
                    topLevelLogSink?.writeLog(LogLevel.from(level), LogDomain.from(domain), message.toKString()!!)
                }
            }
        }
    }
