package com.couchbase.lite.kmp

internal class DocumentReplicationListenerHolder(
    val listener: DocumentReplicationListener,
    val replicator: Replicator
)
