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
import kotbase.internal.fleece.toKString
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import libcblite.CBLFileLogSink
import libcblite.kCBLLogNone
import platform.posix.strdup
import platform.posix.strlen

internal fun CValue<CBLFileLogSink>.asFileLogSink(): FileLogSink? {
    return useContents {
        if (level.toUInt() == kCBLLogNone) {
            null
        } else {
            FileLogSink(
                LogLevel.from(level),
                directory.toKString()!!,
                usePlaintext,
                maxKeptFiles.toInt(),
                maxSize.toLong()
            )
        }
    }
}

internal val FileLogSink?.actual: CValue<CBLFileLogSink>
    get() = cValue {
        level = this@actual?.level?.actual ?: kCBLLogNone.convert()
        directory.buf = this@actual?.directory?.let { strdup(it) }
        directory.size = this@actual?.directory?.let { strlen(it) } ?: 0U
        usePlaintext = this@actual?.isPlainText ?: false
        maxKeptFiles = this@actual?.maxKeptFiles?.convert() ?: 0U
        maxSize = this@actual?.maxFileSize?.convert() ?: 0UL
    }
