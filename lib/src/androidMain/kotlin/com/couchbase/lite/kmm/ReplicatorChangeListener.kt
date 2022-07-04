package com.couchbase.lite.kmm

internal fun ReplicatorChangeListener.convert(): com.couchbase.lite.ReplicatorChangeListener {
    return com.couchbase.lite.ReplicatorChangeListener { change ->
        changed(ReplicatorChange(change))
    }
}
