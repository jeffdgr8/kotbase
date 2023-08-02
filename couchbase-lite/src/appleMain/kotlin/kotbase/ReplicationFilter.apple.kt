package kotbase

import cocoapods.CouchbaseLite.CBLReplicationFilter

internal fun ReplicationFilter.convert(): CBLReplicationFilter {
    return { document, flags ->
        invoke(Document(document!!), flags.toDocumentFlags())
    }
}

internal fun CBLReplicationFilter.convert(): ReplicationFilter {
    return { document, flags ->
        this!!.invoke(document.actual, flags.toCBLDocumentFlags())
    }
}
