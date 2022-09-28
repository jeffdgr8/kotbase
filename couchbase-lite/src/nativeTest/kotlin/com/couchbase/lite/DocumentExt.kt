package com.couchbase.lite

import com.couchbase.lite.kmp.Dictionary
import com.couchbase.lite.kmp.Document

internal actual val Document.content: Dictionary
    get() = Dictionary(properties)

// TODO: expose internal native functions to implement

internal actual fun Document.exists(): Boolean {
    return false
}

internal actual fun Document.generation(): Long =
    0L
