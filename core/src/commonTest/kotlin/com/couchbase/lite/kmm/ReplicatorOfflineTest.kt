package com.couchbase.lite.kmm

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
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
        withTimeout(LONG_TIMEOUT_SEC.seconds) {
            offline.lock()
        }
        withTimeout(LONG_TIMEOUT_SEC.seconds) {
            stopped.lock()
        }
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
        withTimeout(LONG_TIMEOUT_SEC.seconds) {
            stopped.lock()
        }
        repl.removeChangeListener(token)
    }
}
