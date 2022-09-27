package com.couchbase.lite.kmp.internal.fleece

import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import libcblite.*

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

internal fun FLArray.getValue(index: Int): FLValue? =
    FLArray_Get(this, index.convert())
