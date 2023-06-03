package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorChange

internal fun ReplicatorChangeListener.convert(
    replicator: Replicator
): (CBLReplicatorChange?) -> Unit {
    return { change ->
        invoke(ReplicatorChange(change!!, replicator))
    }
}
