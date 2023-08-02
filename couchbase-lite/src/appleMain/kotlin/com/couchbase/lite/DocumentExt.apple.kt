package com.couchbase.lite

import cocoapods.CouchbaseLite.generation
import kotbase.Document

internal actual val Document.generation: Long
    get() = actual.generation.toLong()
