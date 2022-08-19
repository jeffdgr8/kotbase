package com.couchbase.lite.kmp

public expect class Conflict {

    public val documentId: String

    public val localDocument: Document?

    public val remoteDocument: Document?
}
