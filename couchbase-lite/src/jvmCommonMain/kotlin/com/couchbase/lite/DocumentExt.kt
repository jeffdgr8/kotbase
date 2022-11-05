@file:JvmName("DocumentExtJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite

import com.couchbase.lite.kmp.Document

internal actual val Document.generation: Long
    get() = actual.generation()
