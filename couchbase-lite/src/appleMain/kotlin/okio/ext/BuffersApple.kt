/*
 * Copyright (C) 2020 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// TODO: workaround until these extensions are merged and released in Okio
//  https://github.com/square/okio/pull/1123
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "INVISIBLE_SETTER")

package okio.ext

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import okio.Buffer
import okio.SegmentPool
import platform.posix.memcpy
import platform.posix.uint8_tVar

internal fun Buffer.read(sink: CPointer<uint8_tVar>, maxLength: Int): Int {
    require(maxLength >= 0) { "maxLength ($maxLength) must not be negative" }

    val s = head ?: return 0
    val toCopy = minOf(maxLength, s.limit - s.pos)
    s.data.usePinned {
        memcpy(sink, it.addressOf(s.pos), toCopy.convert())
    }

    s.pos += toCopy
    size -= toCopy.toLong()

    if (s.pos == s.limit) {
        head = s.pop()
        SegmentPool.recycle(s)
    }

    return toCopy
}
