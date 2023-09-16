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
package kotbase.internal.fleece

import kotbase.ext.toByteArray
import kotlinx.cinterop.CValue
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.cstr
import kotlinx.cinterop.useContents
import libcblite.FLSliceResult_Release
import libcblite.FLStr
import libcblite.FLString
import libcblite.FLStringResult
import platform.posix.strdup

internal fun FLString.toKString(): String? =
    buf?.toByteArray(size.toInt())?.decodeToString()

internal fun CValue<FLString>.toKString(): String? =
    useContents { toKString() }

private fun FLStringResult.toKString(): String? =
    buf?.toByteArray(size.toInt())?.decodeToString()

internal fun CValue<FLStringResult>.toKString(): String? {
    val result = useContents { toKString() }
    FLSliceResult_Release(this)
    return result
}

// TODO: ensure all usages of this actually take ownership of heap allocated C string
internal fun String?.toFLString(): CValue<FLString> =
    FLStr(this?.let { strdup(it) })

// TODO: ensure all usages don't access string past the scope
internal fun String?.toFLString(memScope: MemScope): CValue<FLString> =
    FLStr(this?.cstr?.getPointer(memScope))
