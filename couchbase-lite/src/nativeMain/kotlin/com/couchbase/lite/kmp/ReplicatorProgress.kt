package com.couchbase.lite.kmp

import libcblite.CBLReplicatorProgress

public actual class ReplicatorProgress
internal constructor(internal val actual: CBLReplicatorProgress) {

    public actual val completed: Long
        get() = actual.documentCount.toLong()

    public actual val total: Long by lazy {
        (completed / actual.complete).toLong()
    }
}
