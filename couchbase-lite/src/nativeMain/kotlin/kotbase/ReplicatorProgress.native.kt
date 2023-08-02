package kotbase

import libcblite.CBLReplicatorProgress

public actual class ReplicatorProgress
internal constructor(actual: CBLReplicatorProgress) {

    public actual val completed: Long = actual.documentCount.toLong()

    public actual val total: Long = (completed / actual.complete).toLong()

    override fun toString(): String = "Progress{completed=$completed, total=$total}"
}
