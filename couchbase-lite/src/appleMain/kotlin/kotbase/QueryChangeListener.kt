package kotbase

import cocoapods.CouchbaseLite.CBLQueryChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun QueryChangeListener.convert(): (CBLQueryChange?) -> Unit {
    return { change ->
        invoke(QueryChange(change!!))
    }
}

internal fun QueryChangeSuspendListener.convert(scope: CoroutineScope): (CBLQueryChange?) -> Unit {
    return { change ->
        scope.launch {
            invoke(QueryChange(change!!))
        }
    }
}
