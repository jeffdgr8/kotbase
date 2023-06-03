package kotbase

import cocoapods.CouchbaseLite.CBLDatabaseChange

internal fun DatabaseChangeListener.convert(): (CBLDatabaseChange?) -> Unit {
    return { change ->
        invoke(DatabaseChange(change!!))
    }
}
