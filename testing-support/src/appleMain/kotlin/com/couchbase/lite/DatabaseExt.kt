@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.couchbase.lite

import cocoapods.CouchbaseLite.isClosed
import kotbase.Database

actual val Database.isOpen: Boolean
    get() = !actual.isClosed()

actual fun <R> Database.withDbLock(action: () -> R): R =
    withLock(action)
