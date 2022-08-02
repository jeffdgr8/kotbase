package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLReplicatorStatus
import com.couchbase.lite.kmp.ext.toCouchbaseLiteException
import com.udobny.kmp.DelegatedClass

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
