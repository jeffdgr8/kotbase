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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

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

    val error by err

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

        if (stopStates.contains(level)) {
            latch.countDown()
        }
    }

    suspend fun awaitCompletion(maxWait: Duration = BaseTest.LONG_TIMEOUT_SEC.seconds): Boolean =
        token.use {
            val ok = latch.await(maxWait)
            if (!ok) err.compareAndSet(null, IllegalStateException("timeout"))
            return ok
        }
}

internal class ReplicatorAwaiter(private val repl: Replicator, coroutineContext: CoroutineContext) : ReplicatorChangeListener {
    private val awaiter = ListenerAwaiter(repl.addChangeListener(coroutineContext, this))

    val error: Throwable?
        get() = awaiter.error

    override fun invoke(change: ReplicatorChange) = awaiter.changed(change)

    suspend fun awaitCompletion(maxWait: Duration = BaseTest.LONG_TIMEOUT_SEC.seconds): Boolean {
        Report.log("Awaiting replicator $repl")
        val ok = awaiter.awaitCompletion(maxWait)
        Report.log("Replicator finished ($ok, ${awaiter.error}): $repl")
        return ok
    }
}

// A filter can actually hang the replication
internal class DelayFilter(val name: String, private val barrier: CyclicBarrier) : ReplicationFilter {
    private val shouldWait = atomic(true)

    override fun invoke(doc: Document, flags: Set<DocumentFlag>): Boolean = runBlocking {
        if (shouldWait.getAndSet(false)) {
            Report.log("$name in delay with doc: ${doc.id}")
            try {
                barrier.await(BaseTest.STD_TIMEOUT_SEC.seconds)
            } catch (e: Exception) {
                Report.log("$name delay interrupted", e)
            }
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

    protected fun ReplicatorConfiguration.run(code: Int = 0, reset: Boolean = false): Replicator {
        return this.testReplicator().run(
            reset,
            expectedErrs = if (code == 0) emptyArray() else arrayOf(
                CouchbaseLiteException("", CBLError.Domain.CBLITE, code)
            )
        )
    }

    protected fun Replicator.run(code: Int = 0): Replicator {
        return this.run(
            expectedErrs = if (code == 0) emptyArray() else arrayOf(
                CouchbaseLiteException("", CBLError.Domain.CBLITE, code)
            )
        )
    }

    protected fun Replicator.run(
        reset: Boolean = false,
        timeoutSecs: Long = LONG_TIMEOUT_SEC,
        vararg expectedErrs: Exception
    ): Replicator = runBlocking {
        val awaiter = ReplicatorAwaiter(this@run, testSerialCoroutineContext)

        Report.log("Test replicator starting: $config")
        try {
            this@run.start(reset)
            if (!awaiter.awaitCompletion(timeoutSecs.seconds)) {
                throw AssertionError("Replicator timed out")
            }
        } finally {
            this@run.stop()
        }

        val err = awaiter.error
        if (err == null) {
            if (expectedErrs.isNotEmpty()) {
                throw AssertionError("Replication finished successfully when expecting error in: $expectedErrs")
            }
        } else {
            if (!containsWithComparator(expectedErrs.toList(), err, BaseTest::compareExceptions)) {
                // Cause isn't logged on native platforms...
                // https://youtrack.jetbrains.com/issue/KT-62794
                println("Cause:")
                println(err.message)
                println(err.stackTraceToString())
                if (expectedErrs.isNotEmpty()) {
                    throw AssertionError("Expecting error in ${expectedErrs.toList()} but got", err)
                } else {
                    throw AssertionError("Expecting no error but got", err)
                }
            }
        }

        this@run
    }
}
