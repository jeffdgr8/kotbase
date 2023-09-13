package com.couchbase.lite

import kotbase.Dictionary
import kotbase.Document
import kotbase.actual

internal actual val Document.content: Dictionary
    get() = Dictionary(actual.content)

internal actual fun Document.exists(): Boolean =
    actual.exists()
