package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLReplicatorChange
import com.udobny.kmm.DelegatedClass

public actual class ReplicatorChange
internal constructor(actual: CBLReplicatorChange) :
    DelegatedClass<CBLReplicatorChange>(actual) {

    public actual val replicator: Replicator by lazy {
        Replicator(actual.replicator!!)
    }

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status!!)
    }
}
