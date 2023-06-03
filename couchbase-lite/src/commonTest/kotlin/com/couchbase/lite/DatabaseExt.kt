package com.couchbase.lite

import kotbase.Blob
import kotbase.Database

internal expect val Database.dbPath: String?

internal expect fun Database.saveBlob(blob: Blob)

internal expect fun Database.getBlob(props: Map<String, Any?>): Blob?

internal fun Database.copy(): Database =
    Database(name, config)

internal expect fun Database.getC4Document(id: String): C4Document

internal expect class C4Document {

    fun isRevDeleted(): Boolean
}
