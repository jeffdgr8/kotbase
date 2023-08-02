@file:JvmName("ReplicationChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.couchbase.lite.ReplicatorChangeListener as CBLReplicatorChangeListener

internal fun ReplicatorChangeListener.convert(replicator: Replicator): CBLReplicatorChangeListener =
    CBLReplicatorChangeListener { change ->
        invoke(ReplicatorChange(change, replicator))
    }

internal fun ReplicatorChangeSuspendListener.convert(
    replicator: Replicator,
    scope: CoroutineScope
): CBLReplicatorChangeListener = CBLReplicatorChangeListener { change ->
    scope.launch {
        invoke(ReplicatorChange(change, replicator))
    }
}
