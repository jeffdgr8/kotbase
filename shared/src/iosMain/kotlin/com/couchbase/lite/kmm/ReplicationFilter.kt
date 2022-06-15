package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLReplicationFilter

internal fun ReplicationFilter.convert(): CBLReplicationFilter {
    return { document, flags ->
        filtered(Document(document!!), flags.toDocumentFlags())
    }
}

internal fun CBLReplicationFilter.convert(): ReplicationFilter {
    return ReplicationFilter { document, flags ->
        this!!.invoke(document.actual, flags.toCBLDocumentFlags())
    }
}
