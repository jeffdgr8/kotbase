package com.couchbase.lite.kmp

import libcblite.CBLReplicatorProgress

public actual class ReplicatorProgress
internal constructor(internal val actual: CBLReplicatorProgress) {

    public actual val completed: Long
        get() = actual.complete.toLong()

    public actual val total: Long
        get() = actual.documentCount.toLong()
}
