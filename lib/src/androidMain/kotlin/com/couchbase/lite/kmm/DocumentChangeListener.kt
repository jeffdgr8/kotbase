package com.couchbase.lite.kmm

internal fun DocumentChangeListener.convert(): com.couchbase.lite.DocumentChangeListener {
    return com.couchbase.lite.DocumentChangeListener { change ->
        changed(DocumentChange(change))
    }
}
