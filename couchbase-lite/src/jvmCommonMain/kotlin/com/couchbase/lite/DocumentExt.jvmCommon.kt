package com.couchbase.lite

import kotbase.Document
import kotbase.actual

internal actual val Document.generation: Long
    get() = actual.generation()
