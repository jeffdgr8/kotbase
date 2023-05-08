package com.couchbase.lite.kmp

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class DatabaseEETest : BaseReplicatorTest() {

    // DatabaseTest.swift

    // TODO: https://issues.couchbase.com/browse/CBL-3526
    @Test
    fun testCloseWithActiveReplicators() = runBlocking {
        // Live Queries:

        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)

        val ds = DataSource.database(baseTestDb)

        val q1 = QueryBuilder.select().from(ds)
        q1.addChangeListener { mutex1.unlock() }

        val q2 = QueryBuilder.select().from(ds)
        q2.addChangeListener { mutex2.unlock() }

        withTimeout(5.seconds) {
            mutex1.lock()
            mutex2.lock()
        }

        // Replicators:

        val target = DatabaseEndpoint(otherDB)
        val config = ReplicatorConfiguration(baseTestDb, target)
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

        withTimeout(5.seconds) {
            idle1.lock()
            idle2.lock()
        }

        baseTestDb.close()

        withTimeout(5.seconds) {
            stopped1.lock()
            stopped2.lock()
        }
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

    // TODO: https://issues.couchbase.com/browse/CBL-3526
    @Test
    fun testCloseWithActiveLiveQueriesAndReplicators() = runBlocking {
        val target = DatabaseEndpoint(otherDB)
        val config = ReplicatorConfiguration(baseTestDb, target)
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

        withTimeout(5.seconds) {
            idle1.lock()
            idle2.lock()
        }

        baseTestDb.close()

        withTimeout(5.seconds) {
            stopped1.lock()
            stopped2.lock()
        }
    }
}
