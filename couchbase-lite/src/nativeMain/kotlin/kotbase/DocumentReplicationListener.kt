package kotbase

import kotlinx.coroutines.CoroutineScope

internal sealed class DocumentReplicationListenerHolder(
    val replicator: Replicator
)

internal class DocumentReplicationDefaultListenerHolder(
    val listener: DocumentReplicationListener,
    replicator: Replicator
) : DocumentReplicationListenerHolder(replicator)

internal class DocumentReplicationSuspendListenerHolder(
    val listener: DocumentReplicationSuspendListener,
    replicator: Replicator,
    val scope: CoroutineScope
) : DocumentReplicationListenerHolder(replicator)
