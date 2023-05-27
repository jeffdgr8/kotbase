package com.couchbase.lite

import com.couchbase.lite.kmp.Database

expect val Database.isOpen: Boolean

expect fun <R> Database.withDbLock(action: () -> R): R
