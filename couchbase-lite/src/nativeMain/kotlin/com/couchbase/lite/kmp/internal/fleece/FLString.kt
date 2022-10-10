package com.couchbase.lite.kmp.internal.fleece

import com.udobny.kmp.ext.toByteArray
import kotlinx.cinterop.*
import libcblite.FLSliceResult_Release
import libcblite.FLStr
import libcblite.FLString
import libcblite.FLStringResult
import platform.posix.strdup
import platform.posix.strlen

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
