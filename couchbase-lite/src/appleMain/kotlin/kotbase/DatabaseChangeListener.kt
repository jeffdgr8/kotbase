package kotbase

import cocoapods.CouchbaseLite.CBLDatabaseChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun DatabaseChangeListener.convert(): (CBLDatabaseChange?) -> Unit {
    return { change ->
        invoke(DatabaseChange(change!!))
    }
}

internal fun DatabaseChangeSuspendListener.convert(scope: CoroutineScope): (CBLDatabaseChange?) -> Unit {
    return { change ->
        scope.launch {
            invoke(DatabaseChange(change!!))
        }
    }
}
