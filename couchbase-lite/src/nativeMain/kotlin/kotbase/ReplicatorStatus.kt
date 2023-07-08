package kotbase

import kotbase.internal.toException
import kotlinx.cinterop.Arena
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.pointed
import libcblite.CBLReplicatorStatus
import kotlin.native.internal.createCleaner

public actual class ReplicatorStatus {

    private val arena = Arena()

    @Suppress("unused")
    private val cleaner = createCleaner(arena) {
        it.clear()
    }

    public val actual: CPointer<CBLReplicatorStatus>

    internal constructor(actual: CValue<CBLReplicatorStatus>) {
        this.actual = actual.getPointer(arena)
    }

    internal constructor(actual: CPointer<CBLReplicatorStatus>) {
        this.actual = actual
    }

    public actual val activityLevel: ReplicatorActivityLevel
        get() = ReplicatorActivityLevel.from(actual.pointed.activity)

    public actual val progress: ReplicatorProgress
        get() = ReplicatorProgress(actual.pointed.progress)

    public actual val error: CouchbaseLiteException?
        get() = actual.pointed.error.toException()

    override fun toString(): String = "Status{activityLevel=$activityLevel, progress=$progress, error=$error}"
}
