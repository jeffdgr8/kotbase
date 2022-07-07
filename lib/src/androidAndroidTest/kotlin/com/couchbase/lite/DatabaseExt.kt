package com.couchbase.lite

import com.couchbase.lite.kmm.Database

internal actual val Database.isOpen: Boolean
    get() = actual.isOpen

internal actual fun <R> Database.withLock(action: () -> R): R {
    return synchronized(actual.dbLock, action)
}

internal actual val Database.dbPath: String?
    get() = actual.dbPath
