//// TODO:
//package com.couchbase.lite.kmm
//
//import com.couchbase.lite.kmm.internal.utils.Report.log
//import com.couchbase.lite.mock.TestReplicatorChangeListener
//import kotlin.test.*
//
//abstract class BaseReplicatorTest : BaseDbTest() {
//
//    protected var baseTestReplicator: Replicator? = null
//    protected var otherDB: Database? = null
//
//    @BeforeTest
//    @Throws(CouchbaseLiteException::class)
//    fun setUpBaseReplicatorTest() {
//        otherDB = createDb("replicator_db")
//        log(LogLevel.INFO, "Create other DB: $otherDB")
//        assertNotNull(otherDB)
//        synchronized(otherDB.getDbLock()) { assertTrue(otherDB.isOpen()) }
//    }
//
//    @AfterTest
//    fun tearDownBaseReplicatorTest() {
//        deleteDb(otherDB)
//        log(LogLevel.INFO, "Deleted other DB: $otherDB")
//    }
//
//    @get:Throws(java.net.URISyntaxException::class)
//    protected val remoteTargetEndpoint: URLEndpoint
//        protected get() = URLEndpoint(java.net.URI("ws://foo.couchbase.com/db"))
//
//    // Don't let the NetworkConnectivityManager confuse tests
//    protected fun testReplicator(config: ReplicatorConfiguration?): Replicator {
//        return Replicator(null, config)
//    }
//
//    protected fun makeConfig(
//        target: Endpoint?,
//        type: ReplicatorType,
//        continuous: Boolean,
//        pinnedServerCert: java.security.cert.Certificate? = null
//    ): ReplicatorConfiguration {
//        return makeConfig(baseTestDb, target, type, continuous, pinnedServerCert)
//    }
//
//    protected fun makeConfig(
//        source: Database?,
//        target: Endpoint?,
//        type: ReplicatorType,
//        continuous: Boolean,
//        pinnedServerCert: java.security.cert.Certificate?,
//        resolver: ConflictResolver?
//    ): ReplicatorConfiguration {
//        val config = makeConfig(source, target, type, continuous, pinnedServerCert)
//        if (resolver != null) {
//            config.conflictResolver = resolver
//        }
//        return config
//    }
//
//    protected fun makeConfig(
//        source: Database?,
//        target: Endpoint?,
//        type: ReplicatorType,
//        continuous: Boolean,
//        pinnedServerCert: java.security.cert.Certificate?
//    ): ReplicatorConfiguration {
//        val config = makeConfig(source, target, type, continuous)
//        val pin: ByteArray?
//        pin = try {
//            if (pinnedServerCert == null) null else pinnedServerCert.getEncoded()
//        } catch (e: java.security.cert.CertificateEncodingException) {
//            throw IllegalArgumentException("Invalid pinned server certificate", e)
//        }
//        config.pinnedServerCertificate = pin
//        return config
//    }
//
//    protected fun makeConfig(
//        source: Database,
//        target: Endpoint,
//        type: ReplicatorType,
//        continuous: Boolean
//    ): ReplicatorConfiguration {
//        return ReplicatorConfiguration(source, target).apply {
//            this.type = type
//            this.isContinuous = continuous
//            this.heartbeat = DISABLE_HEARTBEAT
//        }
//    }
//
//    protected fun run(
//        config: ReplicatorConfiguration?,
//        expectedErrorCode: Int = 0,
//        expectedErrorDomain: String? = null,
//        reset: Boolean = false,
//        onReady: ((Replicator) -> Unit)? = null
//    ): Replicator {
//        return run(
//            testReplicator(config),
//            expectedErrorCode,
//            expectedErrorDomain,
//            reset,
//            onReady
//        )
//    }
//
//    private fun run(
//        repl: Replicator,
//        expectedErrorCode: Int = 0,
//        expectedErrorDomain: String? = null,
//        reset: Boolean = false,
//        onReady: ((Replicator) -> Unit)? = null
//    ): Replicator {
//        baseTestReplicator = repl
//        val listener = TestReplicatorChangeListener()
//        onReady?.invoke(repl)
//        val token = repl.addChangeListener(testSerialExecutor, listener)
//        log("Test replicator starting: " + repl.config)
//        val success: Boolean
//        success = try {
//            repl.start(reset)
//            listener.awaitCompletion(STD_TIMEOUT_SEC, java.util.concurrent.TimeUnit.SECONDS)
//        } finally {
//            repl.removeChangeListener(token)
//        }
//        val err: Throwable = listener.getError()
//        log(err, "Test replicator finished: $success")
//        if (expectedErrorCode == 0 && expectedErrorDomain == null) {
//            if (err != null) {
//                throw RuntimeException(err)
//            }
//        } else {
//            assertNotNull(err)
//            if (err !is CouchbaseLiteException) {
//                throw RuntimeException(err)
//            }
//            val cblErr = err
//            if (expectedErrorCode != 0) {
//                assertEquals(expectedErrorCode.toLong(), cblErr.getCode().toLong())
//            }
//            if (expectedErrorDomain != null) {
//                assertEquals(expectedErrorDomain, cblErr.getDomain())
//            }
//        }
//        return repl
//    }
//}
