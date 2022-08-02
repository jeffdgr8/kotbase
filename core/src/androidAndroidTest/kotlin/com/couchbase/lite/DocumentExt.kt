package com.couchbase.lite

import com.couchbase.lite.kmp.Dictionary
import com.couchbase.lite.kmp.Document

internal actual val Document.content: Dictionary
    get() = Dictionary(actual.content)

internal actual fun Document.exists(): Boolean =
    actual.exists()

internal actual fun Document.generation(): Long =
    actual.generation()
