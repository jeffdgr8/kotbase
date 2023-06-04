@file:JvmName("ReplicationChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

import com.couchbase.lite.ReplicatorChangeListener as CBLReplicatorChangeListener

internal fun ReplicatorChangeListener.convert(
    replicator: Replicator
): CBLReplicatorChangeListener =
    CBLReplicatorChangeListener { change ->
        invoke(ReplicatorChange(change, replicator))
    }
