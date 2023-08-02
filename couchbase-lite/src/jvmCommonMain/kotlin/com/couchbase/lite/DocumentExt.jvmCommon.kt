package com.couchbase.lite

import kotbase.Document

internal actual val Document.generation: Long
    get() = actual.generation()
