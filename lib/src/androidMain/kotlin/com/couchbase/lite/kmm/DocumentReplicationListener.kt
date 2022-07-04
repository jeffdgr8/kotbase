package com.couchbase.lite.kmm

internal fun DocumentReplicationListener.convert(): com.couchbase.lite.DocumentReplicationListener {
    return com.couchbase.lite.DocumentReplicationListener { replication ->
        replication(DocumentReplication(replication))
    }
}
