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

class DatabaseEETest : BaseReplicatorTest() {

    // DatabaseTest.swift

    @Test
    fun testCloseWithActiveReplicators() = runBlocking {
        // Live Queries:

        val change1 = Mutex(true)
        val change2 = Mutex(true)

        val ds = DataSource.database(testDatabase)

        val q1 = QueryBuilder.select().from(ds)
        q1.addChangeListener { change1.unlock() }

        val q2 = QueryBuilder.select().from(ds)
        q2.addChangeListener { change2.unlock() }

        assertTrue(change1.lockWithTimeout(5.seconds))
        assertTrue(change2.lockWithTimeout(5.seconds))

        // Replicators:

        val target = DatabaseEndpoint(targetDatabase)
        val config = ReplicatorConfiguration(testDatabase, target)
        config.isContinuous = true

        config.type = ReplicatorType.PUSH
        val r1 = Replicator(config)
        val idle1 = Mutex(true)
        val stopped1 = Mutex(true)
        startReplicator(r1, idle1, stopped1)

        config.type = ReplicatorType.PULL
        val r2 = Replicator(config)
        val idle2 = Mutex(true)
        val stopped2 = Mutex(true)
        startReplicator(r2, idle2, stopped2)

        assertTrue(idle1.lockWithTimeout(5.seconds))
        assertTrue(idle2.lockWithTimeout(5.seconds))

        testDatabase.close()

        assertTrue(stopped1.lockWithTimeout(5.seconds))
        assertTrue(stopped2.lockWithTimeout(5.seconds))
    }

    private fun startReplicator(replicator: Replicator, idleMutex: Mutex, stoppedMutex: Mutex) {
        replicator.addChangeListener { change ->
            when (change.status.activityLevel) {
                ReplicatorActivityLevel.IDLE -> if (idleMutex.isLocked) idleMutex.unlock()
                ReplicatorActivityLevel.STOPPED -> stoppedMutex.unlock()
                else -> {}
            }
        }
        replicator.start()
    }

    @Test
    fun testCloseWithActiveLiveQueriesAndReplicators() = runBlocking {
        val target = DatabaseEndpoint(targetDatabase)
        val config = ReplicatorConfiguration(testDatabase, target)
        config.isContinuous = true

        config.type = ReplicatorType.PUSH
        val r1 = Replicator(config)
        val idle1 = Mutex(true)
        val stopped1 = Mutex(true)
        startReplicator(r1, idle1, stopped1)

        config.type = ReplicatorType.PULL
        val r2 = Replicator(config)
        val idle2 = Mutex(true)
        val stopped2 = Mutex(true)
        startReplicator(r2, idle2, stopped2)

        assertTrue(idle1.lockWithTimeout(5.seconds))
        assertTrue(idle2.lockWithTimeout(5.seconds))

        testDatabase.close()

        assertTrue(stopped1.lockWithTimeout(5.seconds))
        assertTrue(stopped2.lockWithTimeout(5.seconds))
    }
}
