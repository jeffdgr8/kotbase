package com.couchbase.lite.kmp.internal.fleece

import kotlinx.cinterop.*
import libcblite.FLSlice
import libcblite.FLSliceResult
import libcblite.FLSliceResult_Release
import platform.posix.free
import platform.posix.memcpy

private fun FLSlice.toByteArray(): ByteArray = ByteArray(size.toInt()).apply {
    if (isNotEmpty()) {
        usePinned {
            memcpy(it.addressOf(0), buf, this@toByteArray.size)
        }
    }
}

internal fun CValue<FLSlice>.toByteArray(): ByteArray =
    useContents { toByteArray() }

private fun FLSliceResult.toByteArray(): ByteArray = ByteArray(size.toInt()).apply {
    if (isNotEmpty()) {
        usePinned {
            memcpy(it.addressOf(0), buf, this@toByteArray.size)
        }
    }
}

internal fun CValue<FLSliceResult>.toByteArray(): ByteArray {
    val result = useContents { toByteArray() }
    FLSliceResult_Release(this)
    return result
}

internal fun ByteArray.toFLSlice(): CValue<FLSlice> =
    cValue {
        buf = nativeHeap.allocArrayOf(this@toFLSlice)
        size = this@toFLSlice.size.convert()
    }
