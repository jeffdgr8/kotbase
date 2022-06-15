package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDocumentReplication

internal fun DocumentReplicationListener.convert(): (CBLDocumentReplication?) -> Unit {
    return { replication ->
        replication(DocumentReplication(replication!!))
    }
}
