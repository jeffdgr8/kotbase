package com.couchbase.lite

import cocoapods.CouchbaseLite.generation
import kotbase.Document
import kotbase.actual

internal actual val Document.generation: Long
    get() = actual.generation.toLong()
