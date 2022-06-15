package com.couchbase.lite.kmm

internal fun ConflictHandler.convert(): com.couchbase.lite.ConflictHandler {
    return com.couchbase.lite.ConflictHandler { document, oldDocument ->
        handle(MutableDocument(document), oldDocument?.asDocument())
    }
}
