package com.udobny.kmp.ext

import kotlinx.cinterop.*
import platform.posix.memcpy

public fun CPointer<ByteVar>.toByteArray(size: Int): ByteArray {
    return ByteArray(size).apply {
        if (isNotEmpty()) {
            memcpy(refTo(0), this@toByteArray, size.convert())
        }
    }
}

public fun COpaquePointer.toByteArray(size: Int): ByteArray =
    reinterpret<ByteVar>().toByteArray(size)
