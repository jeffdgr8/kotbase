package kotbase

import kotlinx.coroutines.CoroutineScope

internal sealed class ReplicatorChangeListenerHolder(
    val replicator: Replicator
)

internal class ReplicatorChangeDefaultListenerHolder(
    val listener: ReplicatorChangeListener,
    replicator: Replicator
) : ReplicatorChangeListenerHolder(replicator)

internal class ReplicatorChangeSuspendListenerHolder(
    val listener: ReplicatorChangeSuspendListener,
    replicator: Replicator,
    val scope: CoroutineScope
) : ReplicatorChangeListenerHolder(replicator)
