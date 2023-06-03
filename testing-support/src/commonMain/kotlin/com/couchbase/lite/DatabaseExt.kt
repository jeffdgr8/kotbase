package com.couchbase.lite

import kotbase.Database

expect val Database.isOpen: Boolean

expect fun <R> Database.withDbLock(action: () -> R): R
