@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.couchbase.lite

import com.couchbase.lite.kmp.Database

actual val Database.isOpen: Boolean
    get() = !isClosed

actual fun <R> Database.withDbLock(action: () -> R): R =
    withLock(action)
