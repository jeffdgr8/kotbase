package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicatorChange
import com.udobny.kmp.DelegatedClass

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
