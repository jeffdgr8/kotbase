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
package kotbase.ext

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.*
import platform.Foundation.NSData
import platform.Foundation.data
import platform.Foundation.dataWithBytes
import platform.posix.memcpy

public fun ByteArray.toNSData(): NSData {
    return if (isNotEmpty()) {
        usePinned {
            NSData.dataWithBytes(it.addressOf(0), length = size.convert())
        }
    } else {
        NSData.data()
    }
}

public fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        if (isNotEmpty()) {
            memcpy(refTo(0), bytes, length)
        }
    }
}

public fun ByteArray.toCFData(): CFDataRef {
    return CFDataCreate(
        null,
        asUByteArray().refTo(0),
        size.convert()
    )!!
}

public fun CFDataRef.toByteArray(): ByteArray {
    val length = CFDataGetLength(this)
    return UByteArray(length.toInt()).apply {
        val range = CFRangeMake(0, length)
        CFDataGetBytes(this@toByteArray, range, refTo(0))
    }.asByteArray()
}
