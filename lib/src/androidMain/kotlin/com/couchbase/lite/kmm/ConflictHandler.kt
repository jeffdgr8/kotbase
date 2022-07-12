@file:JvmName("ConflictHandlerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmm

internal fun ConflictHandler.convert(): com.couchbase.lite.ConflictHandler {
    return com.couchbase.lite.ConflictHandler { document, oldDocument ->
        invoke(MutableDocument(document), oldDocument?.asDocument())
    }
}
