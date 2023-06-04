package kotbase

import com.couchbase.lite.ReplicatorType as CBLReplicatorType

public actual enum class ReplicatorType {
    PUSH_AND_PULL,
    PUSH,
    PULL;

    public val actual: CBLReplicatorType
        get() = when (this) {
            PUSH_AND_PULL -> CBLReplicatorType.PUSH_AND_PULL
            PUSH -> CBLReplicatorType.PUSH
            PULL -> CBLReplicatorType.PULL
        }

    internal companion object {

        internal fun from(replicatorType: CBLReplicatorType): ReplicatorType = when (replicatorType) {
            CBLReplicatorType.PUSH_AND_PULL -> PUSH_AND_PULL
            CBLReplicatorType.PUSH -> PUSH
            CBLReplicatorType.PULL -> PULL
        }
    }
}
