package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLReplicatorChange

internal fun ReplicatorChangeListener.convert(): (CBLReplicatorChange?) -> Unit {
    return { change ->
        changed(ReplicatorChange(change!!))
    }
}
