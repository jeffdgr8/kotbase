package com.couchbase.lite

import cocoapods.CouchbaseLite.generation
import com.couchbase.lite.kmp.Document

internal actual val Document.generation: Long
    get() = actual.generation.toLong()
