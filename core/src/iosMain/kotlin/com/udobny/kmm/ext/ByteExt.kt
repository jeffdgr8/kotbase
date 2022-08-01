@file:OptIn(ExperimentalUnsignedTypes::class)

package com.udobny.kmm.ext

import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

public fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = size.convert())
}

public fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).apply {
    if (isNotEmpty()) {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
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
