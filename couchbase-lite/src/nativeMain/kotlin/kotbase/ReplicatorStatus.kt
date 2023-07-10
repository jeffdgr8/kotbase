package kotbase

import kotbase.internal.toException
import kotlinx.cinterop.*
import libcblite.CBLReplicatorStatus

public actual class ReplicatorStatus {

    internal constructor(actual: CPointer<CBLReplicatorStatus>) {
        activityLevel = ReplicatorActivityLevel.from(actual.pointed.activity)
        progress = ReplicatorProgress(actual.pointed.progress)
        error = actual.pointed.error.toException()
    }

    internal constructor(actual: CValue<CBLReplicatorStatus>) {
        lateinit var tempActivity: ReplicatorActivityLevel
        lateinit var tempProgress: ReplicatorProgress
        var tempError: CouchbaseLiteException? = null
        actual.useContents {
            tempActivity = ReplicatorActivityLevel.from(activity)
            tempProgress = ReplicatorProgress(progress)
            tempError = error.toException()
        }
        activityLevel = tempActivity
        progress = tempProgress
        error = tempError
    }

    public actual val activityLevel: ReplicatorActivityLevel

    public actual val progress: ReplicatorProgress

    public actual val error: CouchbaseLiteException?

    override fun toString(): String = "Status{activityLevel=$activityLevel, progress=$progress, error=$error}"
}
