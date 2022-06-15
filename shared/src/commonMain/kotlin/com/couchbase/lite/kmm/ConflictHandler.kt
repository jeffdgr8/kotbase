package com.couchbase.lite.kmm

public fun interface ConflictHandler {

    public fun handle(document: MutableDocument, oldDocument: Document?): Boolean
}
