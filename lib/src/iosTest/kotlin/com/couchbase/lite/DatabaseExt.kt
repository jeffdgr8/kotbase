package com.couchbase.lite

import com.couchbase.lite.kmm.Database
import platform.objc.objc_sync_enter
import platform.objc.objc_sync_exit

internal actual val Database.isOpen: Boolean
    get() = isOpen

internal actual fun <R> Database.withLock(action: () -> R): R {
    objc_sync_enter(actual)
    val ret = action()
    objc_sync_exit(actual)
    return ret
}

internal actual val Database.dbPath: String?
    get() {
        // CBLDatabase.databasePath(name, dir)
        val name = name.replace('/', ':') + ".cblite2" // kDBExtension
        val dir = config.getDirectory().dropLastWhile { it == '/' }
        return "$dir/$name"
    }
