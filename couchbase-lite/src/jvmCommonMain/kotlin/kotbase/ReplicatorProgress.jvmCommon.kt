package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.ReplicatorProgress as CBLReplicatorProgress

public actual class ReplicatorProgress
internal constructor(actual: CBLReplicatorProgress) : DelegatedClass<CBLReplicatorProgress>(actual) {

    public actual val completed: Long
        get() = actual.completed

    public actual val total: Long
        get() = actual.total
}
