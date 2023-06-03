@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.couchbase.lite

import kotbase.Database

actual val Database.isOpen: Boolean
    get() = !isClosed

actual fun <R> Database.withDbLock(action: () -> R): R =
    withLock(action)
