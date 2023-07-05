package kotbase

import cocoapods.CouchbaseLite.CBLDocumentChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun DocumentChangeListener.convert(): (CBLDocumentChange?) -> Unit {
    return { change ->
        invoke(DocumentChange(change!!))
    }
}

internal fun DocumentChangeSuspendListener.convert(scope: CoroutineScope): (CBLDocumentChange?) -> Unit {
    return { change ->
        scope.launch {
            invoke(DocumentChange(change!!))
        }
    }
}
