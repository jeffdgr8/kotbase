package com.couchbase.lite.kmp.internal.fleece

import kotlinx.cinterop.memScoped
import libcblite.FLArray
import libcblite.FLMutableArray_AppendString
import libcblite.FLMutableArray_New

internal fun FLArray.toList(): List<Any?> {
    return buildList {
        memScoped {
            this@toList.iterator(this).forEach {
                add(it.toObject())
            }
        }
    }
}

internal fun List<String>.toFLArray(): FLArray {
    return FLMutableArray_New()!!.apply {
        forEach {
            FLMutableArray_AppendString(this, it.toFLString())
        }
    }
}
