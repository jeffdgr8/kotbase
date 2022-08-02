@file:Suppress("MemberVisibilityCanBePrivate")

package com.couchbase.lite.kmm

import com.couchbase.lite.isOpen
import com.couchbase.lite.kmm.internal.utils.Report
import com.couchbase.lite.kmm.mock.TestReplicatorChangeListener
import com.couchbase.lite.withLock
import kotlinx.coroutines.runBlocking
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

abstract class BaseReplicatorTest : BaseDbTest() {

    protected var baseTestReplicator: Replicator? = null
    protected var otherDB: Database? = null

    @BeforeTest
    fun setUpBaseReplicatorTest() {
        otherDB = createDb("replicator_db")
        Report.log("Create other DB: $otherDB")
        assertNotNull(otherDB)
        otherDB!!.withLock {
            assertTrue(otherDB!!.isOpen)
        }
    }

    @AfterTest
    fun tearDownBaseReplicatorTest() {
        deleteDb(otherDB)
        Report.log("Deleted other DB: $otherDB")
    }

    protected val remoteTargetEndpoint: URLEndpoint
        get() = URLEndpoint("ws://foo.couchbase.com/db")

    protected fun makeConfig(
        target: Endpoint,
        type: ReplicatorType,
        continuous: Boolean
    ): ReplicatorConfiguration =
        makeConfig(baseTestDb, target, type, continuous)

    protected fun makeConfig(
        source: Database,
        target: Endpoint,
        type: ReplicatorType,
        continuous: Boolean
    ): ReplicatorConfiguration {
        return ReplicatorConfiguration(source, target).apply {
            this.type = type
            isContinuous = continuous
            heartbeat = DISABLE_HEARTBEAT
        }
    }

    protected fun run(
        config: ReplicatorConfiguration,
        expectedErrorCode: Int = 0,
        expectedErrorDomain: String? = null,
        reset: Boolean = false,
        onReady: ((Replicator) -> Unit)? = null
    ): Replicator =
        run(testReplicator(config), expectedErrorCode, expectedErrorDomain, reset, onReady)

    private fun run(
        repl: Replicator,
        expectedErrorCode: Int = 0,
        expectedErrorDomain: String? = null,
        reset: Boolean = false,
        onReady: ((Replicator) -> Unit)? = null
    ): Replicator = runBlocking {
        baseTestReplicator = repl

        val listener = TestReplicatorChangeListener()

        onReady?.invoke(repl)

        val token = repl.addChangeListener(listener)

        Report.log("Test replicator starting: " + repl.config)
        val success = try {
            repl.start(reset)
            listener.awaitCompletion(STD_TIMEOUT_SEC.seconds)
        } finally {
            repl.removeChangeListener(token)
        }

        val err = listener.error
        Report.log("Test replicator finished: $success", err)

        if (expectedErrorCode == 0 && expectedErrorDomain == null) {
            if (err != null) {
                throw RuntimeException(err)
            }
        } else {
            assertNotNull(err)
            if (err !is CouchbaseLiteException) {
                throw RuntimeException(err)
            }
            if (expectedErrorCode != 0) {
                assertEquals(expectedErrorCode, err.getCode())
            }
            if (expectedErrorDomain != null) {
                assertEquals(expectedErrorDomain, err.getDomain())
            }
        }

        repl
    }

    companion object {

        // Don't let the NetworkConnectivityManager confuse tests
        fun testReplicator(config: ReplicatorConfiguration): Replicator {
            return Replicator(config, true)
        }
    }
}
