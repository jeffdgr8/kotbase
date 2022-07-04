package com.couchbase.lite.kmm

internal fun DatabaseChangeListener.convert(): com.couchbase.lite.DatabaseChangeListener {
    return com.couchbase.lite.DatabaseChangeListener { change ->
        changed(DatabaseChange(change))
    }
}
