package com.couchbase.lite.kmp.internal.fleece

import kotlinx.cinterop.memScoped
import libcblite.FLArray

internal fun FLArray.toList(): List<Any?> {
    return buildList {
        memScoped {
            this@toList.iterator(this).forEach {
                add(it.toObject())
            }
        }
    }
}
