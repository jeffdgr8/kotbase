package com.couchbase.lite.kmm

import kotlinx.cinterop.*
import platform.Foundation.NSFileManager

actual fun dirExists(dir: String): Boolean {
    return memScoped {
        val isDir = alloc<BooleanVar>()
        NSFileManager.defaultManager.fileExistsAtPath(dir, isDir.ptr) && isDir.value
    }
}
