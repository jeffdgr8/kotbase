//// TODO: uses 3.1 APIs
//package com.couchbase.lite.kmp
//
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class ReplicatorMiscTest : BaseReplicatorTest() {
//
//    @Test
//    fun testGetExecutor() {
//        val executor: java.util.concurrent.Executor =
//            java.util.concurrent.Executor { runnable: java.lang.Runnable? -> }
//        val listener: ReplicatorChangeListener =
//            ReplicatorChangeListener { change: com.couchbase.lite.ReplicatorChange? -> }
//
//        // custom Executor
//        var token = ReplicatorChangeListenerToken(executor, listener) { t -> }
//        assertEquals(executor, token.getExecutor())
//
//        // UI thread Executor
//        token = ReplicatorChangeListenerToken(null, listener) { t -> }
//        assertEquals(
//            CouchbaseLiteInternal.getExecutionService().getDefaultExecutor(),
//            token.getExecutor()
//        )
//    }
//
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
//        val status: ReplicatorStatus = ReplicatorStatus(c4ReplicatorStatus)
//        val repChange: ReplicatorChange = ReplicatorChange(baseTestReplicator, status)
//        assertEquals(repChange.replicator, baseTestReplicator)
//        assertEquals(repChange.status, status)
//        assertEquals(repChange.status.activityLevel, status.activityLevel)
//        assertEquals(repChange.status.progress.completed, completed)
//        assertEquals(repChange.status.progress.total, total)
//        assertEquals(
//            repChange.status.error!!.code,
//            errorCode
//        )
//        assertEquals(repChange.status.error!!.domain, CBLError.Domain.CBLITE)
//    }
//
//    @Test
//    fun testDocumentReplication() {
//        val isPush = true
//        val docs = mutableListOf<ReplicatedDocument>()
//        val doc: DocumentReplication = DocumentReplication(baseTestReplicator, isPush, docs)
//        assertEquals(doc.isPush, isPush)
//        assertEquals(doc.replicator, baseTestReplicator)
//        assertEquals(doc.documents, docs)
//    }
//
//    // https://issues.couchbase.com/browse/CBL-89
//    // Thanks to @James Flather for the ready-made test code
//    @Test
//    fun testStopBeforeStart() {
//        testReplicator(makeConfig(remoteTargetEndpoint, ReplicatorType.PUSH, false)).stop()
//    }
//
//    // https://issues.couchbase.com/browse/CBL-88
//    // Thanks to @James Flather for the ready-made test code
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testStatusBeforeStart() {
//        testReplicator(
//            makeConfig(
//                remoteTargetEndpoint,
//                ReplicatorType.PUSH,
//                false
//            )
//        ).getStatus()
//    }
//
//    @Test(expected = java.lang.IllegalArgumentException::class)
//    @Throws(java.net.URISyntaxException::class)
//    fun testIllegalMaxAttempts() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).maxAttempts = -1
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testMaxAttemptsZero() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).maxAttempts = 0
//    }
//
//    @Test(expected = java.lang.IllegalArgumentException::class)
//    @Throws(java.net.URISyntaxException::class)
//    fun testIllegalAttemptsWaitTime() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).maxAttemptWaitTime = -1
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testMaxAttemptsWaitTimeZero() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).maxAttemptWaitTime = 0
//    }
//
//    @Test(expected = java.lang.IllegalArgumentException::class)
//    @Throws(java.net.URISyntaxException::class)
//    fun testIllegalHeartbeatMin() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).heartbeat = -1
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testHeartbeatZero() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).heartbeat = 0
//    }
//
//    @Test(expected = java.lang.IllegalArgumentException::class)
//    @Throws(java.net.URISyntaxException::class)
//    fun testIllegalHeartbeatMax() {
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint).heartbeat = 2147484
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testDocumentEndListenerTokenRemove() {
//        val repl: Replicator =
//            testReplicator(ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint))
//        assertEquals(0, repl.getDocEndListenerCount())
//        val token = repl.addDocumentReplicationListener { r: DocumentReplication? -> }
//        assertEquals(1, repl.getDocEndListenerCount())
//        token.remove()
//        assertEquals(0, repl.getDocEndListenerCount())
//        token.remove()
//        assertEquals(0, repl.getDocEndListenerCount())
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun tesReplicationListenerTokenRemove() {
//        val repl: Replicator =
//            testReplicator(ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint))
//        assertEquals(0, repl.getReplicatorListenerCount())
//        val token = repl.addChangeListener { r: ReplicatorChange? -> }
//        assertEquals(1, repl.getReplicatorListenerCount())
//        token.remove()
//        assertEquals(0, repl.getReplicatorListenerCount())
//        token.remove()
//        assertEquals(0, repl.getReplicatorListenerCount())
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testDefaultConnectionOptions() {
//        // Don't use makeConfig: it sets the heartbeat
//        val config: ReplicatorConfiguration = also {
//            ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//                .type = it
//        }
//            .setContinuous(false)
//        val repl: Replicator = testReplicator(config)
//        val options: MutableMap<String, Any> = java.util.HashMap<String, Any>()
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
//        run(
//            repl,
//            CBLError.Code.NETWORK_OFFSET + C4Constants.NetworkError.UNKNOWN_HOST,
//            CBLError.Domain.CBLITE
//        )
//        synchronized(options) {
//            assertNull(options[C4Replicator.REPLICATOR_OPTION_ENABLE_AUTO_PURGE])
//            assertFalse(options.containsKey(C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL))
//            assertFalse(options.containsKey(C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL))
//            assertFalse(options.containsKey(C4Replicator.REPLICATOR_OPTION_MAX_RETRIES))
//        }
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testCustomConnectionOptions() {
//        val config: ReplicatorConfiguration = also {
//            ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//                .type = it
//        }
//            .setContinuous(false)
//            .setHeartbeat(33)
//            .setMaxAttempts(78)
//            .setMaxAttemptWaitTime(45)
//            .setAutoPurgeEnabled(false)
//        val repl: Replicator = testReplicator(config)
//        val options: MutableMap<String, Any> = java.util.HashMap<String, Any>()
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
//        run(
//            repl,
//            CBLError.Code.NETWORK_OFFSET + C4Constants.NetworkError.UNKNOWN_HOST,
//            CBLError.Domain.CBLITE
//        )
//        synchronized(options) {
//            assertEquals(
//                java.lang.Boolean.FALSE,
//                options[C4Replicator.REPLICATOR_OPTION_ENABLE_AUTO_PURGE]
//            )
//            assertEquals(
//                33L,
//                options[C4Replicator.REPLICATOR_HEARTBEAT_INTERVAL]
//            )
//            assertEquals(
//                45L,
//                options[C4Replicator.REPLICATOR_OPTION_MAX_RETRY_INTERVAL]
//            )
//            /* A friend once told me: Don't try to teach a pig to sing.  It won't work and it annoys the pig. */assertEquals(
//            78L - 1L,
//            options[C4Replicator.REPLICATOR_OPTION_MAX_RETRIES]
//        )
//        }
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testStopWhileConnecting() {
//        val repl: Replicator =
//            testReplicator(makeConfig(remoteTargetEndpoint, ReplicatorType.PUSH, false))
//        val latch: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        val token = repl.addChangeListener { status: ReplicatorChange ->
//            if (status.status.activityLevel === ReplicatorActivityLevel.CONNECTING) {
//                repl.stop()
//                latch.countDown()
//            }
//        }
//        repl.start()
//        try {
//            try {
//                assertTrue(
//                    latch.await(
//                        STD_TIMEOUT_SEC,
//                        java.util.concurrent.TimeUnit.SECONDS
//                    )
//                )
//            } catch (ignore: java.lang.InterruptedException) {
//            }
//        } finally {
//            repl.stop()
//        }
//    }
//
//    @Test
//    fun testReplicatedDocument() {
//        val collection: Collection<*> = baseTestDb.getDefaultCollection()
//        val docID = "someDocumentID"
//        val flags: Int = C4Constants.DocumentFlags.DELETED
//        val error = CouchbaseLiteException(
//            "Replicator busy",
//            CBLError.Domain.CBLITE,
//            CBLError.Code.BUSY
//        )
//        val doc: ReplicatedDocument = ReplicatedDocument(
//            collection.getScope().getName(),
//            collection.getName(),
//            docID,
//            flags,
//            error
//        )
//        assertEquals(doc.id, docID)
//        assertTrue(doc.flags.contains(DocumentFlag.DELETED))
//        val err = doc.error
//        assertEquals(CBLError.Domain.CBLITE, err!!.domain)
//        assertEquals(CBLError.Code.BUSY, err!!.code)
//        assertEquals(java.util.Collection.DEFAULT_NAME, doc.getCollectionName())
//    }
//
//    // CBL-1218
//    @Test(expected = java.lang.IllegalStateException::class)
//    @Throws(java.net.URISyntaxException::class)
//    fun testStartReplicatorWithClosedDb() {
//        val replicator: Replicator = testReplicator(
//            makeConfig(
//                baseTestDb,
//                remoteTargetEndpoint,
//                ReplicatorType.PUSH_AND_PULL,
//                false,
//                null,
//                null
//            )
//        )
//        closeDb(baseTestDb)
//        replicator.start(false)
//    }
//
//    // CBL-1218
//    @Test(expected = java.lang.IllegalStateException::class)
//    @Throws(
//        CouchbaseLiteException::class,
//        java.net.URISyntaxException::class
//    )
//    fun testIsDocumentPendingWithClosedDb() {
//        val replicator: Replicator = testReplicator(
//            makeConfig(
//                baseTestDb,
//                remoteTargetEndpoint,
//                ReplicatorType.PUSH_AND_PULL,
//                false,
//                null,
//                null
//            )
//        )
//        closeDb(baseTestDb)
//        replicator.getPendingDocumentIds()
//    }
//
//    // CBL-1218
//    @Test(expected = java.lang.IllegalStateException::class)
//    @Throws(
//        CouchbaseLiteException::class,
//        java.net.URISyntaxException::class
//    )
//    fun testGetPendingDocIdsWithClosedDb() {
//        val doc = MutableDocument()
//        otherDB.save(doc)
//        val replicator: Replicator = testReplicator(
//            makeConfig(
//                baseTestDb,
//                remoteTargetEndpoint,
//                ReplicatorType.PUSH_AND_PULL,
//                false,
//                null,
//                null
//            )
//        )
//        closeDb(baseTestDb)
//        replicator.isDocumentPending(doc.id)
//    }
//
//    // CBL-1441
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
//
//    // Verify that deprecated and new ReplicatorTypes are interchangeable
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testDeprecatedReplicatorType() {
//        val config = ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//        assertEquals(
//            AbstractReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL,
//            config.getReplicatorType()
//        )
//        assertEquals(ReplicatorType.PUSH_AND_PULL, config.type)
//        config.setReplicatorType(AbstractReplicatorConfiguration.ReplicatorType.PUSH)
//        assertEquals(
//            AbstractReplicatorConfiguration.ReplicatorType.PUSH,
//            config.getReplicatorType()
//        )
//        assertEquals(ReplicatorType.PUSH, config.type)
//        config.setReplicatorType(AbstractReplicatorConfiguration.ReplicatorType.PULL)
//        assertEquals(
//            AbstractReplicatorConfiguration.ReplicatorType.PULL,
//            config.getReplicatorType()
//        )
//        assertEquals(ReplicatorType.PULL, config.type)
//        config.type = ReplicatorType.PUSH
//        assertEquals(
//            AbstractReplicatorConfiguration.ReplicatorType.PUSH,
//            config.getReplicatorType()
//        )
//        assertEquals(ReplicatorType.PUSH, config.type)
//        config.type = ReplicatorType.PULL
//        assertEquals(
//            AbstractReplicatorConfiguration.ReplicatorType.PULL,
//            config.getReplicatorType()
//        )
//        assertEquals(ReplicatorType.PULL, config.type)
//    }
//
//    /**
//     * The 4 tests below test replicator cookies option when specifying replicator configuration
//     */
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testReplicatorWithBothAuthenticationAndHeaderCookies() {
//        val authenticator: Authenticator = SessionAuthenticator("mysessionid")
//        val header: java.util.HashMap<String, String> = java.util.HashMap<String, String>()
//        header.put(AbstractCBLWebSocket.HEADER_COOKIES, "region=nw; city=sf")
//        val configuration: ReplicatorConfiguration = authenticator.also {
//            ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//                .authenticator = it
//        }
//            .setHeaders(header)
//        val immutableConfiguration = ImmutableReplicatorConfiguration(configuration)
//        val options: java.util.HashMap<String, Any> =
//            immutableConfiguration.getConnectionOptions() as java.util.HashMap<String, Any>
//
//        // cookie option contains both sgw cookie and user specified cookie
//        val cookies = options.get(C4Replicator.REPLICATOR_OPTION_COOKIES) as String
//        assertNotNull(cookies)
//        assertTrue(cookies.contains("SyncGatewaySession=mysessionid"))
//        assertTrue(cookies.contains("region=nw; city=sf"))
//
//        // user specified cookie should be removed from extra header
//        val httpHeaders: java.util.HashMap<String, Any> =
//            options.get(C4Replicator.REPLICATOR_OPTION_EXTRA_HEADERS) as java.util.HashMap<String, Any>
//        assertNotNull(httpHeaders) //httpHeaders must at least include a mapping for User-Agent
//        assertFalse(httpHeaders.containsKey(AbstractCBLWebSocket.HEADER_COOKIES))
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testReplicatorWithNoCookie() {
//        val immutableConfiguration = ImmutableReplicatorConfiguration(
//            ReplicatorConfiguration(
//                baseTestDb,
//                remoteTargetEndpoint
//            )
//        )
//        val options: java.util.HashMap<String, Any> =
//            immutableConfiguration.getConnectionOptions() as java.util.HashMap<String, Any>
//        assertFalse(options.containsKey(C4Replicator.REPLICATOR_OPTION_COOKIES))
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testReplicatorWithOnlyAuthenticationCookie() {
//        val authenticator: Authenticator = SessionAuthenticator("mysessionid")
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//            .authenticator = authenticator
//        val configuration: ReplicatorConfiguration? =
//            ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//                .authenticator
//        val immutableConfiguration = ImmutableReplicatorConfiguration(configuration)
//        val options: java.util.HashMap<String, Any> =
//            immutableConfiguration.getConnectionOptions() as java.util.HashMap<String, Any>
//        assertEquals(
//            "SyncGatewaySession=mysessionid",
//            options.get(C4Replicator.REPLICATOR_OPTION_COOKIES)
//        )
//    }
//
//    @Test
//    @Throws(java.net.URISyntaxException::class)
//    fun testReplicatorWithOnlyHeaderCookie() {
//        val header: java.util.HashMap<String, String> = java.util.HashMap<String, String>()
//        header.put(AbstractCBLWebSocket.HEADER_COOKIES, "region=nw; city=sf")
//        ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//            .headers = header
//        val configuration: ReplicatorConfiguration? =
//            ReplicatorConfiguration(baseTestDb, remoteTargetEndpoint)
//                .headers
//        val immutableConfiguration = ImmutableReplicatorConfiguration(configuration)
//        val options: java.util.HashMap<String, Any> =
//            immutableConfiguration.getConnectionOptions() as java.util.HashMap<String, Any>
//        assertEquals(
//            "region=nw; city=sf",
//            options.get(C4Replicator.REPLICATOR_OPTION_COOKIES)
//        )
//        val httpHeaders: java.util.HashMap<String, Any> =
//            options.get(C4Replicator.REPLICATOR_OPTION_EXTRA_HEADERS) as java.util.HashMap<String, Any>
//        assertNotNull(httpHeaders) // httpHeaders must at least include a mapping for User-Agent
//        assertFalse(httpHeaders.containsKey(AbstractCBLWebSocket.HEADER_COOKIES))
//    }
//
//    private fun getActivityLevelFor(activityLevel: Int): ReplicatorActivityLevel {
//        return ReplicatorStatus(C4ReplicatorStatus(activityLevel, 0, 0, 0, 0, 0, 0)).activityLevel
//    }
//}
