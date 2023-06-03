package kotbase

import com.couchbase.lite.ReplicatorProgress
import kotbase.base.DelegatedClass

public actual class ReplicatorProgress
internal constructor(actual: com.couchbase.lite.ReplicatorProgress) :
    DelegatedClass<ReplicatorProgress>(actual) {

    public actual val completed: Long
        get() = actual.completed

    public actual val total: Long
        get() = actual.total
}
