package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicatorChange

internal fun ReplicatorChangeListener.convert(): (CBLReplicatorChange?) -> Unit {
    return { change ->
        invoke(ReplicatorChange(change!!))
    }
}
