@file:JvmName("ConflictHandlerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

internal fun ConflictHandler.convert(): com.couchbase.lite.ConflictHandler {
    return com.couchbase.lite.ConflictHandler { document, oldDocument ->
        invoke(MutableDocument(document), oldDocument?.asDocument())
    }
}
