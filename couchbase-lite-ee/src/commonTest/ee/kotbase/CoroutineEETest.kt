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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class CoroutineEETest : BaseCoroutineTest() {

    private fun runWithReplicator(test: (Replicator) -> Unit) {
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = true)
        val replicator = config.testReplicator()
        val stopped = Mutex(true)
        replicator.addChangeListener(Dispatchers.Default.limitedParallelism(1)) {
            when (it.status.activityLevel) {
                ReplicatorActivityLevel.STOPPED -> if (stopped.isLocked) stopped.unlock()
                else -> if (!stopped.isLocked) stopped.lock()
            }
        }
        test(replicator)
        replicator.stop()
        runBlocking {
            assertTrue(stopped.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        }
    }

    // ReplicatorChange

    @Test
    fun testReplicatorChangeOnCoroutineContext() = runWithReplicator { replicator ->
        testOnCoroutineContext(
            addListener = { context, work ->
                replicator.addChangeListener(context) {
                    work()
                }
                replicator.start()
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testReplicatorChangeCoroutineCanceled() = runWithReplicator { replicator ->
        testCoroutineCanceled(
            addListener = { context, work ->
                replicator.addChangeListener(context) {
                    work()
                }.also {
                    replicator.start()
                }
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testReplicatorChangeCoroutineScopeListenerRemoved() = runWithReplicator { replicator ->
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                replicator.addChangeListener(scope) {
                    work()
                }
                replicator.start()
            },
            listenedChange = {
                saveDocInCollection(MutableDocument("listenedDoc"))
            },
            notListenedChange = {
                saveDocInCollection(MutableDocument("notListenedDoc"))
            }
        )
    }

    // DocumentReplication

    @Test
    fun testDocumentReplicationOnCoroutineContext() = runWithReplicator { replicator ->
        testOnCoroutineContext(
            addListener = { context, work ->
                replicator.addDocumentReplicationListener(context) {
                    work()
                }
                replicator.start()
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testDocumentReplicationCoroutineCanceled() = runWithReplicator { replicator ->
        testCoroutineCanceled(
            addListener = { context, work ->
                replicator.addDocumentReplicationListener(context) {
                    work()
                }.also {
                    replicator.start()
                }
            },
            change = {
                saveDocInCollection(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testDocumentReplicationCoroutineScopeListenerRemoved() = runWithReplicator { replicator ->
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                replicator.addDocumentReplicationListener(scope) {
                    work()
                }
                replicator.start()
            },
            listenedChange = {
                saveDocInCollection(MutableDocument("listenedDoc"))
            },
            notListenedChange = {
                saveDocInCollection(MutableDocument("notListenedDoc"))
            }
        )
    }
}
