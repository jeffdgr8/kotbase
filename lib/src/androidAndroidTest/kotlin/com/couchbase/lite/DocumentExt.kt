package com.couchbase.lite

import com.couchbase.lite.kmm.Dictionary
import com.couchbase.lite.kmm.Document

internal actual val Document.content: Dictionary
    get() = Dictionary(actual.content)

internal actual fun Document.exists(): Boolean =
    actual.exists()
