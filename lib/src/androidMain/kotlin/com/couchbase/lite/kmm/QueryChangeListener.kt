package com.couchbase.lite.kmm

internal fun QueryChangeListener.convert(): com.couchbase.lite.QueryChangeListener {
    return com.couchbase.lite.QueryChangeListener { change ->
        changed(QueryChange(change))
    }
}
