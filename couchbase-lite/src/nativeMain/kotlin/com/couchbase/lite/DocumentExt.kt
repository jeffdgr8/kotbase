package com.couchbase.lite

import com.couchbase.lite.kmp.Document
import com.couchbase.lite.kmp.MutableDocument
import libcblite.CBLDocument_Generation

internal actual val Document.generation: Long
    get() {
        val generation = CBLDocument_Generation(actual).toLong()
        return if (this is MutableDocument) {
            // assume MutableDocument is mutated, which expects
            // incremented generation (good enough for tests)
            generation + 1
        } else {
            generation
        }
    }
