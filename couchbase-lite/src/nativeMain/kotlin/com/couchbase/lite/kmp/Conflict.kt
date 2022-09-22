package com.couchbase.lite.kmp

public actual class Conflict(
    public actual val documentId: String,
    public actual val localDocument: Document?,
    public actual val remoteDocument: Document?
)
