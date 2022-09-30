package com.couchbase.lite.kmp.internal.fleece

import com.udobny.kmp.ext.toByteArray
import kotlinx.cinterop.*
import libcblite.FLSliceResult_Release
import libcblite.FLString
import libcblite.FLStringResult
import platform.posix.strdup
import platform.posix.strlen

internal fun FLString.toKString(): String? =
    buf?.toByteArray(size.toInt())?.decodeToString()

internal fun CValue<FLString>.toKString(): String? =
    useContents { toKString() }

private fun FLStringResult.toKString(): String? =
    buf?.reinterpret<ByteVar>()?.toKString()

internal fun CValue<FLStringResult>.toKString(): String? {
    val result = useContents { toKString() }
    FLSliceResult_Release(this)
    return result
}

// TODO: ensure all usages of this actually take ownership of heap allocated C string
internal fun String?.toFLString(): CValue<FLString> {
    val string = this@toFLString
    return cValue {
        if (string != null) {
            buf = strdup(string)
            size = strlen(string)
        } else {
            buf = null
            size = 0.convert()
        }
    }
}

internal fun String?.toFLString(memScope: MemScope): CValue<FLString> {
    val string = this@toFLString
    return cValue {
        if (string != null) {
            buf = string.cstr.getPointer(memScope)
            size = strlen(string)
        } else {
            buf = null
            size = 0.convert()
        }
    }
}
