@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:JvmName("DatabaseExtTestingSupport") // Disambiguate from :couchbase-lite module file

package com.couchbase.lite

import com.couchbase.lite.kmp.Database

actual val Database.isOpen: Boolean
    get() = actual.isOpen

actual fun <R> Database.withDbLock(action: () -> R): R =
    synchronized(actual.dbLock, action)
