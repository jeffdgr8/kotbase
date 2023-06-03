package kotbase

import kotlinx.cinterop.convert
import libcblite.*

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
        }.convert()

    internal companion object {

        internal fun from(activityLevel: CBLReplicatorActivityLevel): ReplicatorActivityLevel {
            return when (activityLevel.toUInt()) {
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
