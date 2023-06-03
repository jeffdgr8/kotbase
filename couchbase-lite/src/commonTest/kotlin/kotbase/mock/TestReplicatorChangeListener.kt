package kotbase.mock

import kotbase.ReplicatorActivityLevel
import kotbase.ReplicatorChange
import kotbase.ReplicatorChangeListener
import kotbase.internal.utils.Report
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

class TestReplicatorChangeListener : ReplicatorChangeListener {

    private val mutex = Mutex(true)

    var error: Throwable? by atomic(null)

    suspend fun awaitCompletion(timeout: Duration): Boolean {
        return try {
            withTimeout(timeout) {
                mutex.lock()
                true
            }
        } catch (e: TimeoutCancellationException) {
            false
        }
    }

    override fun invoke(change: ReplicatorChange) {
        val status = change.status
        val error = status.error
        val state = status.activityLevel
        Report.log("Test replicator state change: $state", error)

        this.error = error

        if (change.replicator.config.isContinuous
            && status.activityLevel == ReplicatorActivityLevel.IDLE
            && status.progress.completed == status.progress.total
        ) {
            change.replicator.stop()
        }

        if (status.activityLevel == ReplicatorActivityLevel.STOPPED) {
            mutex.unlock()
        }
    }
}
