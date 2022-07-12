package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDocumentReplication

internal fun DocumentReplicationListener.convert(): (CBLDocumentReplication?) -> Unit {
    return { replication ->
        invoke(DocumentReplication(replication!!))
    }
}
