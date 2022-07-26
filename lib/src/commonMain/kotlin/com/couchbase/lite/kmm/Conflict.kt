package com.couchbase.lite.kmm

@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class Conflict {

    public val documentId: String

    public val localDocument: Document?

    public val remoteDocument: Document?
}
