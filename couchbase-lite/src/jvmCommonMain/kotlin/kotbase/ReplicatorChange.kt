package kotbase

import com.couchbase.lite.ReplicatorChange
import kotbase.base.DelegatedClass

public actual class ReplicatorChange
internal constructor(
    actual: com.couchbase.lite.ReplicatorChange,
    public actual val replicator: Replicator
) : DelegatedClass<ReplicatorChange>(actual) {

    public actual val status: ReplicatorStatus by lazy {
        ReplicatorStatus(actual.status)
    }
}
