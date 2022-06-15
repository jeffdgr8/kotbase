package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class ReplicatorProgress
internal constructor(actual: com.couchbase.lite.ReplicatorProgress) :
    DelegatedClass<com.couchbase.lite.ReplicatorProgress>(actual) {

    public actual val completed: Long
        get() = actual.completed

    public actual val total: Long
        get() = actual.total
}
