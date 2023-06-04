package kotbase

import com.couchbase.lite.ReplicatorActivityLevel as CBLReplicatorActivityLevel

public actual enum class ReplicatorActivityLevel {
    STOPPED,
    OFFLINE,
    CONNECTING,
    IDLE,
    BUSY;

    public val actual: CBLReplicatorActivityLevel
        get() = when (this) {
            STOPPED -> CBLReplicatorActivityLevel.STOPPED
            OFFLINE -> CBLReplicatorActivityLevel.OFFLINE
            CONNECTING -> CBLReplicatorActivityLevel.CONNECTING
            IDLE -> CBLReplicatorActivityLevel.IDLE
            BUSY -> CBLReplicatorActivityLevel.BUSY
        }

    internal companion object {

        internal fun from(activityLevel: CBLReplicatorActivityLevel): ReplicatorActivityLevel = when (activityLevel) {
            CBLReplicatorActivityLevel.STOPPED -> STOPPED
            CBLReplicatorActivityLevel.OFFLINE -> OFFLINE
            CBLReplicatorActivityLevel.CONNECTING -> CONNECTING
            CBLReplicatorActivityLevel.IDLE -> IDLE
            CBLReplicatorActivityLevel.BUSY -> BUSY
        }
    }
}
