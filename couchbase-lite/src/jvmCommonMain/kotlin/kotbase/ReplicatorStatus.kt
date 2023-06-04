package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ReplicatorStatus as CBLReplicatorStatus

public actual class ReplicatorStatus
internal constructor(actual: CBLReplicatorStatus) : DelegatedClass<CBLReplicatorStatus>(actual) {

    public actual val activityLevel: ReplicatorActivityLevel by lazy {
        ReplicatorActivityLevel.from(actual.activityLevel)
    }

    public actual val progress: ReplicatorProgress by lazy {
        ReplicatorProgress(actual.progress)
    }

    public actual val error: CouchbaseLiteException?
        get() = actual.error
}
