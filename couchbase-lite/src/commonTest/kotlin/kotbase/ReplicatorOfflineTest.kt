package kotbase

import kotbase.test.lockWithTimeout
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ReplicatorOfflineTest : BaseReplicatorTest() {

    @Test
    fun testStopReplicatorAfterOffline() = runBlocking {
        val target = remoteTargetEndpoint
        val config = makeConfig(baseTestDb, target, ReplicatorType.PULL, true)
        val repl = testReplicator(config)
        val offline = Mutex(true)
        val stopped = Mutex(true)
        val token = repl.addChangeListener { change ->
            val status = change.status
            when (status.activityLevel) {
                ReplicatorActivityLevel.OFFLINE -> {
                    change.replicator.stop()
                    offline.unlock()
                }
                ReplicatorActivityLevel.STOPPED -> stopped.unlock()
                else -> {}
            }
        }
        repl.start(false)
        assertTrue(offline.lockWithTimeout(LONG_TIMEOUT_SEC.seconds))
        assertTrue(stopped.lockWithTimeout(LONG_TIMEOUT_SEC.seconds))
        repl.removeChangeListener(token)
    }

    @Test
    fun testStartSingleShotReplicatorInOffline() = runBlocking {
        val repl = testReplicator(makeConfig(remoteTargetEndpoint, ReplicatorType.PUSH, false))
        val stopped = Mutex(true)
        val token = repl.addChangeListener { change ->
            val status = change.status
            if (status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                stopped.unlock()
            }
        }
        repl.start(false)
        assertTrue(stopped.lockWithTimeout(LONG_TIMEOUT_SEC.seconds))
        repl.removeChangeListener(token)
    }
}
