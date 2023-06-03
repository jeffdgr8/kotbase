package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorChange
import kotbase.base.DelegatedClass

public actual class ReplicatorChange
internal constructor(
    actual: CBLReplicatorChange,
    public actual val replicator: Replicator
) : DelegatedClass<CBLReplicatorChange>(actual) {

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status!!)
    }
}
