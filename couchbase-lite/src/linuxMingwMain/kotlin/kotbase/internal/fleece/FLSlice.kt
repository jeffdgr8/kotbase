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

import kotlinx.cinterop.*
import libcblite.FLSlice
import libcblite.FLSliceResult
import libcblite.FLSliceResult_Release
import platform.posix.malloc
import platform.posix.memcpy

private fun FLSlice.toByteArray(): ByteArray = ByteArray(size.toInt()).apply {
    if (isNotEmpty()) {
        memcpy(refTo(0), buf, this@toByteArray.size)
    }
}

internal fun CValue<FLSlice>.toByteArray(): ByteArray =
    useContents { toByteArray() }

private fun FLSliceResult.toByteArray(): ByteArray = ByteArray(size.toInt()).apply {
    if (isNotEmpty()) {
        memcpy(refTo(0), buf, this@toByteArray.size)
    }
}

internal fun CValue<FLSliceResult>.toByteArray(): ByteArray {
    val result = useContents { toByteArray() }
    debug.FLSliceResult_Release(this)
    return result
}

internal fun ByteArray.toFLSlice(): CValue<FLSlice> {
    return cValue {
        size = this@toFLSlice.size.convert()
        buf = malloc(size)
        if (size > 0U) {
            memcpy(buf, this@toFLSlice.refTo(0), size)
        }
    }
}
