package com.couchbase.lite.kmp.internal.fleece

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
    FLSliceResult_Release(this)
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
