package kotbase

import cocoapods.CouchbaseLite.CBLQueryChange

internal fun QueryChangeListener.convert(): (CBLQueryChange?) -> Unit {
    return { change ->
        invoke(QueryChange(change!!))
    }
}