package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class ReplicatorChange
internal constructor(actual: com.couchbase.lite.ReplicatorChange) :
    DelegatedClass<com.couchbase.lite.ReplicatorChange>(actual) {

    public actual val replicator: Replicator by lazy {
        Replicator(actual.replicator)
    }

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status)
    }
}
