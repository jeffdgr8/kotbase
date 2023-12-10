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
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class ReplicatorMiscTest : BaseReplicatorTest() {

//    @Test
//    fun testGetExecutor() {
//        val executor = Executor { runnable -> }
//        val listener: ReplicatorChangeListener = ReplicatorChangeListener { change -> }
//
//        ReplicatorChangeListenerToken(executor, listener) { t -> }.use { token ->
//            assertEquals(executor, token.getExecutor())
//        }
//        ReplicatorChangeListenerToken(null, listener) { t -> }.use { token ->
//            assertEquals(CouchbaseLiteInternal.getExecutionService().getDefaultExecutor(), token.getExecutor())
//        }
//    }

//    @Test
//    fun testReplicatorChange() {
//        val completed: Long = 10
//        val total: Long = 20
//        val errorCode = CBLError.Code.BUSY
//        val errorDomain = 1 // CBLError.Domain.CBLErrorDomain: LiteCoreDomain
//        val c4ReplicatorStatus = C4ReplicatorStatus(
//            C4ReplicatorStatus.ActivityLevel.CONNECTING,
//            completed,
//            total,
//            1,
//            errorDomain,
//            errorCode,
//            0
//        )
//
//        val repl = makeBasicRepl()
//        val replStatus = ReplicatorStatus(c4ReplicatorStatus)
//        val repChange = ReplicatorChange(repl, replStatus)
//
//        assertEquals(repChange.replicator, repl)
//
//        val status = repChange.status
//        assertNotNull(status)
//        assertEquals(status.activityLevel, status.activityLevel)
//
//        val progress = status.progress
//        assertNotNull(progress)
//        assertEquals(progress.completed, completed)
//        assertEquals(progress.total, total)
//
//        val error = status.error
//        assertNotNull(error)
//        assertEquals(error.code, errorCode)
//        assertEquals(error.domain, CBLError.Domain.CBLITE)
//    }

//    @Test
//    fun testDocumentReplication() {
//        val docs = listOf<ReplicatedDocument>()
//        val repl = makeBasicRepl()
//        val doc = DocumentReplication(repl, true, docs)
//        assertTrue(doc.isPush)
//        assertEquals(doc.replicator, repl)
//        assertEquals(doc.documents, docs)
//    }

    // https://issues.couchbase.com/browse/CBL-89
    // Thanks to @James Flather for the ready-made test code
    @Test
    fun testStopBeforeStart() {
        makeBasicRepl().stop()
    }

    // https://issues.couchbase.com/browse/CBL-88
    // Thanks to @James Flather for the ready-made test code
    @Test
    fun testStatusBeforeStart() {
        makeBasicRepl().status
    }

//    @Test
//    fun testDocumentEndListenerTokenRemove() {
//        val repl = makeBasicRepl()
//        assertEquals(0, repl.getDocEndListenerCount())
//        val token = repl.addDocumentReplicationListener { }
//        assertEquals(1, repl.getDocEndListenerCount())
//        token.remove()
//        assertEquals(0, repl.getDocEndListenerCount())
//        token.remove()
//        assertEquals(0, repl.getDocEndListenerCount())
//    }

//    @Test
//    fun testReplicationListenerTokenRemove() {
//        val repl = makeBasicRepl()
//        assertEquals(0, repl.getReplicatorListenerCount())
//        val token = repl.addChangeListener { }
//        assertEquals(1, repl.getReplicatorListenerCount())
//        token.remove()
//        assertEquals(0, repl.getReplicatorListenerCount())
//        token.remove()
//        assertEquals(0, repl.getReplicatorListenerCount())
//    }

//    @Test
//    fun testDefaultConnectionOptions() {
//        val repl = makeDefaultConfig().testReplicator()
//
//        val options = mutableMapOf<String, Any?>()
//        repl.getSocketFactory().setTestListener { c4Socket ->
//            if (c4Socket == null) {
//                return@setTestListener
//            }
//            synchronized(options) {
//                val opts: Map<String, Any> =
//                    (c4Socket as AbstractCBLWebSocket).getOptions()
//                if (opts != null) {
//                    options.putAll(opts)
//                }
//            }
//        }
//
//        // the replicator will fail because the endpoint is bogus
//        run(repl, false, CBLError.Domain.CBLITE, CBLError.Code.UNKNOWN_HOST)
//
//        synchronized(options) {
//            assertEquals(
//                Defaults.Replicator.ACCEPT_PARENT_COOKIES,
//                options[C4Replicator.REPLICATOR_OPTION_ACCEPT_PARENT_COOKIES]
//            )
//            assertEquals(
//                Defaults.Replicator.ENABLE_AUTO_PURGE,
//                options[C4Replicator.REPLICATOR_OPTION_ENABLE_AUTO_PURGE]
//            )
//            assertEquals(
//                Defaults.Replicator.HEARTBEAT,
//                (options[C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL] as Number?)!!.toInt()
//            )
//            assertEquals(
//                Defaults.Replicator.MAX_ATTEMPT_WAIT_TIME,
//                (options[C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL] as Number?)!!.toInt()
//            )
//            assertEquals(
//                Defaults.Replicator.MAX_ATTEMPTS_SINGLE_SHOT - 1,
//                (options[C4Replicator.REPLICATOR_OPTION_MAX_RETRIES] as Number?)!!.toInt()
//            )
//        }
//    }

//    @Test
//    fun testCustomConnectionOptions() {
//        // Caution: not disabling heartbeat
//        val repl = makeDefaultConfig()
//            .setHeartbeat(33)
//            .setMaxAttempts(78)
//            .setMaxAttemptWaitTime(45)
//            .setAutoPurgeEnabled(false)
//            .setAcceptParentDomainCookies(true).testReplicator()
//
//        val options = mutableMapOf<String, Any?>()
//        repl.getSocketFactory().setTestListener { delegate ->
//            if (delegate !is AbstractCBLWebSocket) {
//                return@setTestListener
//            }
//            synchronized(options) {
//                val opts: Map<String, Any> =
//                    (delegate as AbstractCBLWebSocket).getOptions()
//                if (opts != null) {
//                    options.putAll(opts)
//                }
//            }
//        }
//
//        // the replicator will fail because the endpoint is bogus
//        run(repl, false, CBLError.Domain.CBLITE, CBLError.Code.UNKNOWN_HOST)
//
//        synchronized(options) {
//            assertEquals(java.lang.Boolean.TRUE, options[C4Replicator.REPLICATOR_OPTION_ACCEPT_PARENT_COOKIES])
//            assertEquals(java.lang.Boolean.FALSE, options[C4Replicator.REPLICATOR_OPTION_ENABLE_AUTO_PURGE])
//            assertEquals(33L, options[C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL])
//            assertEquals(45L, options[C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL])
//            // A friend once told me: Don't try to teach a pig to sing.  It won't work and it annoys the pig.
//            assertEquals(78L - 1, options[C4Replicator.REPLICATOR_OPTION_MAX_RETRIES])
//        }
//    }

//    @Test
//    fun testBasicAuthOptions() = runBlocking {
//        val config = makeBasicConfig()
//        config.setAuthenticator(BasicAuthenticator("user", "sekrit".toCharArray()))
//        val repl = config.testReplicator()
//
//        val options = mutableMapOf<String, Any?>()
//        repl.getSocketFactory().setTestListener { c4Socket ->
//            if (c4Socket == null) {
//                return@setTestListener
//            }
//            synchronized(options) {
//                val opts: Map<String, Any> =
//                    (c4Socket as AbstractCBLWebSocket).getOptions()
//                if (opts != null) {
//                    options.putAll(opts)
//                }
//            }
//        }
//
//        // the replicator will fail because the endpoint is bogus
//        repl.run(false, CBLError.Domain.CBLITE, CBLError.Code.UNKNOWN_HOST)
//
//        synchronized(options) {
//            val authOpts = options[C4Replicator.REPLICATOR_OPTION_AUTHENTICATION]
//            assertTrue(authOpts is Map<*, *>)
//            val auth = authOpts as Map<*, *>?
//            assertEquals(C4Replicator.AUTH_TYPE_BASIC, auth!![C4Replicator.REPLICATOR_AUTH_TYPE])
//            assertEquals("sekrit", auth[C4Replicator.REPLICATOR_AUTH_PASSWORD])
//        }
//    }

    @Test
    fun testStopWhileConnecting() = runBlocking {
        val repl = makeBasicRepl()

        val latch = CountDownLatch(1)
        val token = repl.addChangeListener { status ->
            if (status.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
                repl.stop()
                latch.countDown()
            }
        }

        repl.start()
        try {
            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        } finally {
            token.remove()
            repl.stop()
        }
    }

//    @Test
//    fun testReplicatedDocument() {
//        val docId = getUniqueName("replicated-doc")
//        val replicatedDoc = ReplicatedDocument(
//            targetCollection.scope.name,
//            targetCollection.name,
//            docId,
//            C4Constants.DocumentFlags.DELETED,
//            CouchbaseLiteException(
//                "Replicator busy",
//                CBLError.Domain.CBLITE,
//                CBLError.Code.BUSY
//            )
//        )
//
//        assertEquals(replicatedDoc.id, docId)
//
//        assertEquals(targetCollection.scope.name, replicatedDoc.scope)
//        assertEquals(targetCollection.name, replicatedDoc.collection)
//
//        assertTrue(replicatedDoc.flags.contains(DocumentFlag.DELETED))
//
//        val err = replicatedDoc.error
//        assertNotNull(err)
//        assertEquals(CBLError.Domain.CBLITE, err.domain)
//        assertEquals(CBLError.Code.BUSY, err.code)
//    }

    // CBL-1218
    @Test
    fun testStartReplicatorWithClosedDb() {
        val repl = makeBasicRepl()

        closeDb(testDatabase)

        assertFailsWith<IllegalStateException> { repl.start() }
    }

    // CBL-1218
    @Test
    fun testIsDocumentPendingWithClosedDb() {
        val repl = makeBasicRepl()

        deleteDb(testDatabase)

        assertFailsWith<IllegalStateException> { repl.getPendingDocumentIds(testCollection) }
    }

    // CBL-1218
    @Test
    fun testGetPendingDocIdsWithClosedDb() {
        val repl = makeBasicRepl()

        closeDb(testDatabase)

        assertFailsWith<IllegalStateException> {
            repl.isDocumentPending(
                "who-cares",
                testCollection
            )
        }
    }

    // CBL-1441
//    @Test
//    fun testReplicatorStatus() {
//        assertEquals(
//            ReplicatorActivityLevel.BUSY,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.STOPPED - 1)
//        )
//        assertEquals(
//            ReplicatorActivityLevel.STOPPED,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.STOPPED)
//        )
//        assertEquals(
//            ReplicatorActivityLevel.OFFLINE,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.OFFLINE)
//        )
//        assertEquals(
//            ReplicatorActivityLevel.CONNECTING,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.CONNECTING)
//        )
//        assertEquals(
//            ReplicatorActivityLevel.IDLE,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.IDLE)
//        )
//        assertEquals(
//            ReplicatorActivityLevel.BUSY,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.BUSY)
//        )
//        assertEquals(
//            ReplicatorActivityLevel.BUSY,
//            getActivityLevelFor(C4ReplicatorStatus.ActivityLevel.BUSY + 1)
//        )
//    }

    // Verify that deprecated and new ReplicatorTypes are interchangeable
//    @Suppress("deprecation")
//    @Test
//    fun testDeprecatedReplicatorType() {
//        val config = makeDefaultConfig()
//
//        assertEquals(AbstractReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, config.getReplicatorType())
//        assertEquals(ReplicatorType.PUSH_AND_PULL, config.type)
//
//        config.setReplicatorType(AbstractReplicatorConfiguration.ReplicatorType.PUSH)
//        assertEquals(AbstractReplicatorConfiguration.ReplicatorType.PUSH, config.getReplicatorType())
//        assertEquals(ReplicatorType.PUSH, config.type)
//
//        config.setReplicatorType(AbstractReplicatorConfiguration.ReplicatorType.PULL)
//        assertEquals(AbstractReplicatorConfiguration.ReplicatorType.PULL, config.getReplicatorType())
//        assertEquals(ReplicatorType.PULL, config.type)
//
//        config.setType(ReplicatorType.PUSH)
//        assertEquals(AbstractReplicatorConfiguration.ReplicatorType.PUSH, config.getReplicatorType())
//        assertEquals(ReplicatorType.PUSH, config.type)
//
//        config.setType(ReplicatorType.PULL)
//        assertEquals(AbstractReplicatorConfiguration.ReplicatorType.PULL, config.getReplicatorType())
//        assertEquals(ReplicatorType.PULL, config.type)
//    }

    /**
     * The 4 tests below test replicator cookies option when specifying replicator configuration
     */
//    @Test
//    fun testReplicatorWithBothAuthenticationAndHeaderCookies() {
//        val authenticator: Authenticator = SessionAuthenticator("mysessionid")
//        val header = mapOf<String, String>(
//            AbstractCBLWebSocket.HEADER_COOKIES to "region=nw; city=sf"
//        )
//        val configuration = makeDefaultConfig()
//            .setAuthenticator(authenticator)
//            .setHeaders(header)
//
//        val immutableConfiguration: ImmutableReplicatorConfiguration =
//            ImmutableReplicatorConfiguration(configuration)
//        val options: java.util.HashMap<String, Any> =
//            immutableConfiguration.getConnectionOptions() as java.util.HashMap<String, Any>
//
//        // cookie option contains both sgw cookie and user specified cookie
//        val cookies = options.get(C4Replicator.REPLICATOR_OPTION_COOKIES) as String
//        assertNotNull(cookies)
//        assertTrue(cookies.contains("SyncGatewaySession=mysessionid"))
//        assertTrue(cookies.contains("region=nw; city=sf"))
//
//        // user specified cookie should have been removed from extra header
//        val httpHeaders: Any = options.get(C4Replicator.REPLICATOR_OPTION_EXTRA_HEADERS)
//        assertTrue(httpHeaders is Map<*, *>)
//
//        // httpHeaders must at least include a mapping for User-Agent
//        assertFalse((httpHeaders as Map<*, *>).containsKey(AbstractCBLWebSocket.HEADER_COOKIES))
//    }

//    @Test
//    fun testReplicatorWithNoCookie() {
//        val config: ImmutableReplicatorConfiguration =
//            ImmutableReplicatorConfiguration(makeDefaultConfig())
//        val options: Map<*, *> = config.getConnectionOptions()
//        assertFalse(options.containsKey(C4Replicator.REPLICATOR_OPTION_COOKIES))
//    }

//    @Test
//    fun testReplicatorWithOnlyAuthenticationCookie() {
//        assertEquals(
//            "SyncGatewaySession=mysessionid",
//            ImmutableReplicatorConfiguration(
//                makeDefaultConfig().setAuthenticator(SessionAuthenticator("mysessionid"))
//            )
//                .getConnectionOptions()
//                .get(C4Replicator.REPLICATOR_OPTION_COOKIES)
//        )
//    }

//    @Test
//    fun testReplicatorWithOnlyHeaderCookie() {
//        val header = mapOf<String, String>(
//            AbstractCBLWebSocket.HEADER_COOKIES to "region=nw; city=sf"
//        )
//        val configuration = makeDefaultConfig().setHeaders(header)
//
//        val immutableConfiguration: ImmutableReplicatorConfiguration =
//            ImmutableReplicatorConfiguration(configuration)
//        val options: java.util.HashMap<String, Any> =
//            immutableConfiguration.getConnectionOptions() as java.util.HashMap<String, Any>
//
//        assertEquals(
//            "region=nw; city=sf",
//            options.get(C4Replicator.REPLICATOR_OPTION_COOKIES)
//        )
//
//        val httpHeaders: Any = options.get(C4Replicator.REPLICATOR_OPTION_EXTRA_HEADERS)
//        assertTrue(httpHeaders is Map<*, *>)
//
//        // httpHeaders must at least include a mapping for User-Agent
//        assertFalse((httpHeaders as Map<*, *>).containsKey(AbstractCBLWebSocket.HEADER_COOKIES))
//    }


    ///////// Utility functions
//    private fun getActivityLevelFor(activityLevel: Int): ReplicatorActivityLevel {
//        return ReplicatorStatus(C4ReplicatorStatus(activityLevel, 0, 0, 0, 0, 0, 0)).activityLevel
//    }

    // return a nearly default config.
    // Not using makeSimpleReplConfig, in order to make sure this is pure vanilla
    // Note that this does not set heartbeat.  This config is likely to cause flaky tests
    // when used in test with a live replicator
    private fun makeDefaultConfig(): ReplicatorConfiguration {
        return ReplicatorConfiguration(mockURLEndpoint).addCollection(testCollection, null)
    }

    private fun makeBasicConfig(): ReplicatorConfiguration {
        val config = makeDefaultConfig()
        config.setHeartbeat(ReplicatorConfiguration.DISABLE_HEARTBEAT)
        return config
    }

    private fun makeBasicRepl(): Replicator {
        return makeBasicConfig().testReplicator()
    }
}
