package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.DocumentChangeListener as CBLDocumentChangeListener

internal fun DocumentChangeListener.convert(): CBLDocumentChangeListener =
    CBLDocumentChangeListener { change ->
        invoke(DocumentChange(change))
    }

internal fun DocumentChangeSuspendListener.convert(scope: CoroutineScope): CBLDocumentChangeListener =
    CBLDocumentChangeListener { change ->
        scope.launch {
            invoke(DocumentChange(change))
        }
    }
