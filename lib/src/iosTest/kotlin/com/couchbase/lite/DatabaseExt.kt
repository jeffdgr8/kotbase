package com.couchbase.lite

import com.couchbase.lite.kmm.BaseTest.Companion.DB_EXTENSION
import com.couchbase.lite.kmm.Blob
import com.couchbase.lite.kmm.Database
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.ext.wrapError
import platform.Foundation.NSError
import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

internal actual val Database.isOpen: Boolean
    get() = isOpen

internal actual fun <R> Database.withLock(action: () -> R): R {
    objc_sync_enter(actual)
    val result = action()
    objc_sync_exit(actual)
    return result
}

internal actual val Database.dbPath: String?
    get() {
        // CBLDatabase.databasePath(name, dir)
        val name = name.replace('/', ':') + DB_EXTENSION
        val dir = config.getDirectory().dropLastWhile { it == '/' }
        return "$dir/$name"
    }

internal actual fun Database.saveBlob(blob: Blob) {
    wrapError(NSError::toCouchbaseLiteException) { error ->
        actual.saveBlob(blob.actual, error)
    }
}
