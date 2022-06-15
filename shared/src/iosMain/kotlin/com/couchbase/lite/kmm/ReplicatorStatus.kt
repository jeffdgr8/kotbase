package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLReplicatorStatus
import com.couchbase.lite.kmm.ext.toCouchbaseLiteException
import com.udobny.kmm.DelegatedClass

public actual class ReplicatorStatus
internal constructor(actual: CBLReplicatorStatus) :
    DelegatedClass<CBLReplicatorStatus>(actual) {

    public actual val activityLevel: ReplicatorActivityLevel by lazy {
        ReplicatorActivityLevel.from(actual.activity)
    }

    public actual val progress: ReplicatorProgress by lazy {
        ReplicatorProgress(actual.progress)
    }

    public actual val error: CouchbaseLiteException? by lazy {
        actual.error?.toCouchbaseLiteException()
    }
}
