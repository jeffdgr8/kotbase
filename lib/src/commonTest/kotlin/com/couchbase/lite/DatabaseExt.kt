package com.couchbase.lite

import com.couchbase.lite.kmm.Blob
import com.couchbase.lite.kmm.Database

internal expect val Database.isOpen: Boolean

internal expect fun <R> Database.withLock(action: () -> R): R

internal expect val Database.dbPath: String?

internal expect fun Database.saveBlob(blob: Blob)

internal fun Database.copy(): Database =
    Database(name, config)