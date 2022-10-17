package com.couchbase.lite.kmp

public actual class DocumentReplication
internal constructor(
    public actual val replicator: Replicator,
    public actual val isPush: Boolean,
    public actual val documents: List<ReplicatedDocument>
)
