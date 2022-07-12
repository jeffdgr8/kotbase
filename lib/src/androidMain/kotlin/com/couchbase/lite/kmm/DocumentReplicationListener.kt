@file:JvmName("DocumentReplicationListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package com.couchbase.lite.kmm

internal fun DocumentReplicationListener.convert(): com.couchbase.lite.DocumentReplicationListener {
    return com.couchbase.lite.DocumentReplicationListener { replication ->
        invoke(DocumentReplication(replication))
    }
}
