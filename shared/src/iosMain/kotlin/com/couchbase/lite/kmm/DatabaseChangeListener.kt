package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDatabaseChange

internal fun DatabaseChangeListener.convert(): (CBLDatabaseChange?) -> Unit {
    return { change ->
        changed(DatabaseChange(change!!))
    }
}
