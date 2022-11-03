@file:JvmName("DocumentReplicationListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmp

internal fun DocumentReplicationListener.convert(
    replicator: Replicator
): com.couchbase.lite.DocumentReplicationListener {
    return com.couchbase.lite.DocumentReplicationListener { replication ->
        invoke(DocumentReplication(replication, replicator))
    }
}
