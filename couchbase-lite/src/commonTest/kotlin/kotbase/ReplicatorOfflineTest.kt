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

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ReplicatorOfflineTest : BaseReplicatorTest() {

    @Test
    fun testStopReplicatorAfterOffline() = runBlocking {
        val offline = CountDownLatch(1)
        val stopped = CountDownLatch(1)

        val repl = makeConfig().setType(ReplicatorType.PULL).testReplicator()
        val token: ListenerToken = repl.addChangeListener(
            testSerialCoroutineContext
        ) { change ->
            when (change.status.activityLevel) {
                ReplicatorActivityLevel.STOPPED -> stopped.countDown()
                ReplicatorActivityLevel.OFFLINE -> offline.countDown()
                else -> {}
            }
        }

        try {
            repl.start()
            assertTrue(offline.await(LONG_TIMEOUT_SEC.seconds))
            repl.stop()
            assertTrue(stopped.await(LONG_TIMEOUT_SEC.seconds))
        } finally { token.remove() }
    }

    @Test
    fun testStartSingleShotReplicatorInOffline() = runBlocking {
        val stopped = CountDownLatch(1)
        val repl = makeConfig().setContinuous(false).testReplicator()
        val token = repl.addChangeListener(
            testSerialCoroutineContext
        ) { change ->
            if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) { stopped.countDown() }
        }

        try {
            repl.start()
            assertTrue(stopped.await(LONG_TIMEOUT_SEC.seconds))
        }
		finally { token.remove() }
    }

//    @Test
//    fun testAddNullDocumentReplicationListener() {
//        val repl = makeRepl()
//
//        val token = repl.addDocumentReplicationListener { }
//        assertNotNull(token)
//        token.remove()
//
//        assertFailsWith<IllegalArgumentException> { repl.addDocumentReplicationListener(null) }
//    }
//
//    @Test
//    fun testAddNullDocumentReplicationListenerWithExecutor() {
//        val repl = makeRepl()
//
//        val token = repl.addDocumentReplicationListener { }
//        assertNotNull(token)
//        token.remove()
//
//        assertFailsWith<IllegalArgumentException {
//            repl.addDocumentReplicationListener(testSerialCoroutineContext, null)
//        }
//    }
//
//    @Test
//    fun testAddNullChangeListener() {
//        assertFailsWith<IllegalArgumentException> {
//            val token = makeRepl().addChangeListener(null)
//        }
//    }
//
//    @Test
//    fun testNullChangeListenerWithExecutor() {
//        assertFailsWith<IllegalArgumentException> {
//            val token = makeRepl().addChangeListener(testSerialCoroutineContext, null)
//        }
//    }

    private fun makeDefaultConfig(): ReplicatorConfiguration {
        return ReplicatorConfiguration(mockURLEndpoint)
            .addCollection(testCollection, null)
    }

    private fun makeConfig(): ReplicatorConfiguration {
        return makeDefaultConfig()
            .setType(ReplicatorType.PUSH)
            .setContinuous(true)
            .setHeartbeat(ReplicatorConfiguration.DISABLE_HEARTBEAT)
    }

//    private fun makeRepl() = makeConfig().testReplicator()
}
