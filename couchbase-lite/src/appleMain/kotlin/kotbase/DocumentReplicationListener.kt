package kotbase

import cocoapods.CouchbaseLite.CBLDocumentReplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun DocumentReplicationListener.convert(replicator: Replicator): (CBLDocumentReplication?) -> Unit {
    return { replication ->
        invoke(DocumentReplication(replication!!, replicator))
    }
}

internal fun DocumentReplicationSuspendListener.convert(
    replicator: Replicator,
    scope: CoroutineScope
): (CBLDocumentReplication?) -> Unit {
    return { replication ->
        scope.launch {
            invoke(DocumentReplication(replication!!, replicator))
        }
    }
}
