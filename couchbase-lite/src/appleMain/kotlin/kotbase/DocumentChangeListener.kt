package kotbase

import cocoapods.CouchbaseLite.CBLDocumentChange

internal fun DocumentChangeListener.convert(): (CBLDocumentChange?) -> Unit {
    return { change ->
        invoke(DocumentChange(change!!))
    }
}
