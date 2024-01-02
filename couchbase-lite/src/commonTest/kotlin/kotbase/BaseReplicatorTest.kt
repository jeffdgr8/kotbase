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

import kotbase.internal.utils.Report
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.coroutines.sync.CyclicBarrier
import kotlin.coroutines.CoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
internal class ListenerAwaiter(
    private val token: ListenerToken,
    private val stopStates: kotlin.collections.Collection<ReplicatorActivityLevel> = STOP_STATES
) {
    companion object {
        private val STOP_STATES = setOf(
            ReplicatorActivityLevel.STOPPED,
            // behavior of ReplicatorTest.swift
            //ReplicatorActivityLevel.OFFLINE,
            //ReplicatorActivityLevel.IDLE
        )
    }

    private val err = atomic<Throwable?>(null)
    private val latch = CountDownLatch(1)

    val error: Throwable?
        get() = err.value

    fun changed(change: ReplicatorChange) {
        val status = change.status
        status.error?.let { err.compareAndSet(null, it) }
        val level = status.activityLevel
        val e = status.error
        Report.log("Test replicator state change: $level", e)

        if (e != null) err.compareAndSet(null, e)

        // behavior of ReplicatorTest.swift
        val replicator = change.replicator
        if (replicator.config.isContinuous &&
            status.activityLevel == ReplicatorActivityLevel.IDLE &&
            status.progress.completed == status.progress.total
        ) {
            replicator.stop()
        }

        if (stopStates.contains(level)) latch.countDown()
    }

    suspend fun awaitCompletion(maxWait: Duration = BaseTest.LONG_TIMEOUT_SEC.seconds): Boolean =
        token.use {
            val ok = latch.await(maxWait)
            if (!ok) err.compareAndSet(null, IllegalStateException("timeout"))
            return ok
        }
}

internal class ReplicatorAwaiter(repl: Replicator, coroutineContext: CoroutineContext) : ReplicatorChangeListener {
    private val awaiter = ListenerAwaiter(repl.addChangeListener(coroutineContext, this))

    val error: Throwable?
        get() = awaiter.error

    override fun invoke(change: ReplicatorChange) = awaiter.changed(change)

    suspend fun awaitCompletion(maxWait: Duration = BaseTest.LONG_TIMEOUT_SEC.seconds) =
        awaiter.awaitCompletion(maxWait)
}

// A filter can actually hang the replication
internal class DelayFilter(val name: String, private val barrier: CyclicBarrier) : ReplicationFilter {
    private val shouldWait = atomic(true)

    override fun invoke(doc: Document, flags: Set<DocumentFlag>): Boolean = runBlocking {
        if (shouldWait.getAndSet(false)) {
            Report.log("$name waiting with doc: ${doc.id}")
            barrier.await(BaseTest.STD_TIMEOUT_SEC.seconds)
        }

        Report.log("$name filtered doc: ${doc.id}")
        true
    }
}

abstract class BaseReplicatorTest : BaseDbTest() {
    protected val mockURLEndpoint = URLEndpoint("ws://foo.couchbase.com/db")

    protected val nullResolver: ConflictResolver = { null }
    protected val localResolver: ConflictResolver = { conflict -> conflict.localDocument }
    protected val remoteResolver: ConflictResolver = { conflict -> conflict.remoteDocument }

    protected lateinit var targetDatabase: Database
        private set
    protected lateinit var targetCollection: Collection
        private set

    private val replicators = mutableListOf<Replicator>()

    @BeforeTest
    fun setUpBaseReplicatorTest() {
        targetDatabase = createDb("target_db")
        targetCollection = targetDatabase.createSimilarCollection(testCollection)
    }

    @AfterTest
    fun tearDownBaseReplicatorTest() {
        targetCollection.close()
        replicators.forEach { it.close() }
        eraseDb(targetDatabase)
    }

    protected fun makeCollectionConfig(
        channels: List<String>? = null,
        docIds: List<String>? = null,
        pullFilter: ReplicationFilter? = null,
        pushFilter: ReplicationFilter? = null,
        resolver: ConflictResolver? = null
    ): CollectionConfiguration {
        val config = CollectionConfiguration()
        channels?.let { config.channels = it }
        docIds?.let { config.documentIDs = it }
        pullFilter?.let { config.pullFilter = it }
        pushFilter?.let { config.pushFilter = it }
        resolver.let { config.conflictResolver = it }
        return config
    }

    protected fun makeSimpleReplConfig(
        target: Endpoint = mockURLEndpoint,
        source: kotlin.collections.Collection<Collection> = setOf(testCollection),
        srcConfig: CollectionConfiguration? = null,
        type: ReplicatorType? = null,
        continuous: Boolean? = null,
        authenticator: Authenticator? = null,
        headers: Map<String, String>? = null,
        pinnedServerCert: ByteArray? = null,
        maxAttempts: Int = 1,
        maxAttemptWaitTime: Int = 1,
        autoPurge: Boolean = true
    ) = makeReplConfig(
        target,
        mapOf(source to srcConfig),
        type,
        continuous,
        authenticator,
        headers,
        pinnedServerCert,
        maxAttempts,
        maxAttemptWaitTime,
        autoPurge
    )

    protected fun makeReplConfig(
        target: Endpoint = mockURLEndpoint,
        source: Map<out kotlin.collections.Collection<Collection>, CollectionConfiguration?> =
            mapOf(setOf(testCollection) to null),
        type: ReplicatorType? = null,
        continuous: Boolean? = null,
        authenticator: Authenticator? = null,
        headers: Map<String, String>? = null,
        pinnedServerCert: ByteArray? = null,
        maxAttempts: Int = 1,
        maxAttemptWaitTime: Int = 1,
        autoPurge: Boolean = true
    ): ReplicatorConfiguration {
        val config = ReplicatorConfiguration(target)

        source.forEach { config.addCollections(it.key, it.value) }
        type?.let { config.type = it }
        continuous?.let { config.isContinuous = it }
        authenticator?.let { config.setAuthenticator(it) }
        headers?.let { config.headers = it }
        pinnedServerCert?.let { config.pinnedServerCertificate = it }
        maxAttempts.let { config.maxAttempts = it }
        maxAttemptWaitTime.let { config.maxAttemptWaitTime = it }
        autoPurge.let { config.setAutoPurgeEnabled(it) }

        // The mocks used in the loopback tests are
        // not prepared to handle heartbeats
        config.heartbeat = ReplicatorConfiguration.DISABLE_HEARTBEAT

        return config
    }

    // Prefer this method to any other, for creating new replicators
    // It prevents the NetworkConnectivityManager from confusing these tests
    protected fun ReplicatorConfiguration.testReplicator(): Replicator {
        val repl = Replicator(this, true)
        replicators.add(repl)
        return repl
    }

    protected fun ReplicatorConfiguration.run(reset: Boolean = false, errDomain: String? = null, errCode: Int = 0) =
        this.testReplicator().run(reset, errDomain, errCode)

    protected fun Replicator.run(reset: Boolean = false, errDomain: String? = null, errCode: Int = 0): Replicator = runBlocking {
        val awaiter = ReplicatorAwaiter(this@run, testSerialCoroutineContext)

        Report.log("Test replicator starting: $config")
        var ok = false
        try {
            this@run.start(reset)
            ok = awaiter.awaitCompletion(LONG_TIMEOUT_SEC.seconds)
        } finally {
            this@run.stop()
            Report.log("Test replicator ${if (ok) "finished" else "timed out"}", awaiter.error)
        }

        val err = awaiter.error
        if ((errCode == 0) && (errDomain == null)) {
            if (err != null) throw AssertionError("Replication failed with unexpected error", err)
        } else {
            if (err !is CouchbaseLiteException) {
                if (err != null) throw AssertionError("Replication failed with unexpected error", err)
                throw AssertionError("Expected CBLError (${errDomain}, ${errCode}) but no error occurred")
            }

            if (errCode != 0) {
                assertEquals(errCode, err.code)
            }

            if (errDomain != null) {
                assertEquals(errDomain, err.domain)
            }
        }

        this@run
    }
}

class TestConflictResolver(
    // set this resolver, which will be used while resolving the conflict
    val resolver: ConflictResolver
) : ConflictResolver {

    var winner: Document? = null

    override fun invoke(conflict: Conflict): Document? {
        winner = resolver(conflict)
        return winner
    }
}
