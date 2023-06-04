@file:JvmName("DocumentReplicationListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.DocumentReplicationListener as CBLDocumentReplicationListener

internal fun DocumentReplicationListener.convert(replicator: Replicator): CBLDocumentReplicationListener =
    CBLDocumentReplicationListener { replication ->
        invoke(DocumentReplication(replication, replicator))
    }
