package kotbase

import cocoapods.CouchbaseLite.CBLReplicatorActivityLevel
import cocoapods.CouchbaseLite.CBLReplicatorActivityLevel.*

public actual enum class ReplicatorActivityLevel {
    STOPPED,
    OFFLINE,
    CONNECTING,
    IDLE,
    BUSY;

    public val actual: CBLReplicatorActivityLevel
        get() = when (this) {
            STOPPED -> kCBLReplicatorStopped
            OFFLINE -> kCBLReplicatorOffline
            CONNECTING -> kCBLReplicatorConnecting
            IDLE -> kCBLReplicatorIdle
            BUSY -> kCBLReplicatorBusy
        }

    internal companion object {

        internal fun from(activityLevel: CBLReplicatorActivityLevel): ReplicatorActivityLevel {
            return when (activityLevel) {
                kCBLReplicatorStopped -> STOPPED
                kCBLReplicatorOffline -> OFFLINE
                kCBLReplicatorConnecting -> CONNECTING
                kCBLReplicatorIdle -> IDLE
                kCBLReplicatorBusy -> BUSY
                else -> error("Unexpected CBLReplicatorActivityLevel")
            }
        }
    }
}
