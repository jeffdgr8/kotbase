/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
