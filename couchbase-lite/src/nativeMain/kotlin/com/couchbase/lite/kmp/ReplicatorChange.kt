package com.couchbase.lite.kmp

public actual class ReplicatorChange
internal constructor(
    public actual val replicator: Replicator,
    public actual val status: ReplicatorStatus
)
