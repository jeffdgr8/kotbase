package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ReplicatorChange as CBLReplicatorChange

public actual class ReplicatorChange
internal constructor(
    actual: CBLReplicatorChange,
    public actual val replicator: Replicator
) : DelegatedClass<CBLReplicatorChange>(actual) {

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status)
    }
}
