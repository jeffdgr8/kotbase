@file:JvmName("DocumentReplicationListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.DocumentReplicationListener as CBLDocumentReplicationListener

internal fun DocumentReplicationListener.convert(replicator: Replicator): CBLDocumentReplicationListener =
    CBLDocumentReplicationListener { replication ->
        invoke(DocumentReplication(replication, replicator))
    }

internal fun DocumentReplicationSuspendListener.convert(
    replicator: Replicator,
    scope: CoroutineScope
): CBLDocumentReplicationListener = CBLDocumentReplicationListener { replication ->
    scope.launch {
        invoke(DocumentReplication(replication, replicator))
    }
}
