package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorType
import cocoapods.CouchbaseLite.kCBLReplicatorTypePull
import cocoapods.CouchbaseLite.kCBLReplicatorTypePush
import cocoapods.CouchbaseLite.kCBLReplicatorTypePushAndPull

public actual enum class ReplicatorType {
    PUSH_AND_PULL,
    PUSH,
    PULL;

    public val actual: CBLReplicatorType
        get() = when (this) {
            PUSH_AND_PULL -> kCBLReplicatorTypePushAndPull
            PUSH -> kCBLReplicatorTypePush
            PULL -> kCBLReplicatorTypePull
        }

    internal companion object {

        internal fun from(replicatorType: CBLReplicatorType): ReplicatorType = when (replicatorType) {
            kCBLReplicatorTypePushAndPull -> PUSH_AND_PULL
            kCBLReplicatorTypePush -> PUSH
            kCBLReplicatorTypePull -> PULL
            else -> error("Unexpected CBLReplicatorType")
        }
    }
}