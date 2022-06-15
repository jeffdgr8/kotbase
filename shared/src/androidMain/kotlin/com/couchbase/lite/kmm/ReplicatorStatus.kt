package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class ReplicatorStatus
internal constructor(actual: com.couchbase.lite.ReplicatorStatus) :
    DelegatedClass<com.couchbase.lite.ReplicatorStatus>(actual) {

    public actual val activityLevel: ReplicatorActivityLevel by lazy {
        ReplicatorActivityLevel.from(actual.activityLevel)
    }

    public actual val progress: ReplicatorProgress by lazy {
        ReplicatorProgress(actual.progress)
    }

    public actual val error: CouchbaseLiteException?
        get() = actual.error
}
