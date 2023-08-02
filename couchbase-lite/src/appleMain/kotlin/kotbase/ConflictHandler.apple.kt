package kotbase

import cocoapods.CouchbaseLite.CBLDocument
import cocoapods.CouchbaseLite.CBLMutableDocument

internal fun ConflictHandler.convert(): (CBLMutableDocument?, CBLDocument?) -> Boolean {
    return { document, oldDocument ->
        invoke(MutableDocument(document!!), oldDocument?.asDocument())
    }
}
