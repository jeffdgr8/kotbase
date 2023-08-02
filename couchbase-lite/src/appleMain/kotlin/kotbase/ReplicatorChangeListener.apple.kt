package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun ReplicatorChangeListener.convert(replicator: Replicator): (CBLReplicatorChange?) -> Unit {
    return { change ->
        invoke(ReplicatorChange(change!!, replicator))
    }
}

internal fun ReplicatorChangeSuspendListener.convert(
    replicator: Replicator,
    scope: CoroutineScope
): (CBLReplicatorChange?) -> Unit {
    return { change ->
        scope.launch {
            invoke(ReplicatorChange(change!!, replicator))
        }
    }
}
