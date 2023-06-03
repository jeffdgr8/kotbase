@file:JvmName("ReplicationChangeListenerJvm") // https://youtrack.jetbrains.com/issue/KT-21186

package kotbase

internal fun ReplicatorChangeListener.convert(
    replicator: Replicator
): com.couchbase.lite.ReplicatorChangeListener {
    return com.couchbase.lite.ReplicatorChangeListener { change ->
        invoke(ReplicatorChange(change, replicator))
    }
}
