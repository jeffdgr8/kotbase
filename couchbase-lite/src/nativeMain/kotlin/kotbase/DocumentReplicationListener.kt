package kotbase

internal class DocumentReplicationListenerHolder(
    val listener: DocumentReplicationListener,
    val replicator: Replicator
)
