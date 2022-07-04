package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDocument
import cocoapods.CouchbaseLite.CBLMutableDocument

internal fun ConflictHandler.convert(): (CBLMutableDocument?, CBLDocument?) -> Boolean {
    return { document, oldDocument ->
        handle(MutableDocument(document!!), oldDocument?.asDocument())
    }
}
