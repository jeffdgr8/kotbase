package kotbase

import kotlinx.cinterop.convert
import libcblite.CBLReplicatorType
import libcblite.kCBLReplicatorTypePull
import libcblite.kCBLReplicatorTypePush
import libcblite.kCBLReplicatorTypePushAndPull

public actual enum class ReplicatorType {
    PUSH_AND_PULL,
    PUSH,
    PULL;

    internal val actual: CBLReplicatorType
        get() = when (this) {
            PUSH_AND_PULL -> kCBLReplicatorTypePushAndPull
            PUSH -> kCBLReplicatorTypePush
            PULL -> kCBLReplicatorTypePull
        }.convert()

    internal companion object {

        internal fun from(replicatorType: CBLReplicatorType): ReplicatorType = when (replicatorType.toUInt()) {
            kCBLReplicatorTypePushAndPull -> PUSH_AND_PULL
            kCBLReplicatorTypePush -> PUSH
            kCBLReplicatorTypePull -> PULL
            else -> error("Unexpected CBLReplicatorType ($replicatorType)")
        }
    }
}
