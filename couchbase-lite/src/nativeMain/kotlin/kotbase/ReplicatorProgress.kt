package kotbase

import libcblite.CBLReplicatorProgress

public actual class ReplicatorProgress
internal constructor(public val actual: CBLReplicatorProgress) {

    public actual val completed: Long
        get() = actual.documentCount.toLong()

    public actual val total: Long by lazy {
        (completed / actual.complete).toLong()
    }

    override fun toString(): String = "Progress{completed=$completed, total=$total}"
}
