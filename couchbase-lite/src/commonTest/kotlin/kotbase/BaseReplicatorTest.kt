@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase

import kotbase.internal.utils.Report
import kotbase.mock.TestReplicatorChangeListener
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

abstract class BaseReplicatorTest : BaseDbTest() {

    protected var baseTestReplicator: Replicator? = null
    protected lateinit var otherDB: Database

    @BeforeTest
    fun setUpBaseReplicatorTest() {
        otherDB = createDb("replicator_db")
    }

    @AfterTest
    fun tearDownBaseReplicatorTest() {
        otherDB.close()
        baseTestDb.close()
        deleteDb(otherDB)
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
            heartbeat = ReplicatorConfiguration.DISABLE_HEARTBEAT
        }
    }

    // TODO: 3.1. API
//    protected fun makeConfig(
//        target: Endpoint?,
//        source: Set<Collection>,
//        type: ReplicatorType?,
//        continuous: Boolean,
//        pinnedServerCert: ByteArray? = null
//    ): ReplicatorConfiguration {
//        return ReplicatorConfiguration(target)
//            .addCollections(source, null)
//            .setType(type)
//            .setContinuous(continuous)
//            .setHeartbeat(ReplicatorConfiguration.DISABLE_HEARTBEAT).apply {
//                try {
//                    if (pinnedServerCert != null) {
//                        setPinnedServerCertificate(pinnedServerCert)
//                    }
//                } catch (e: Exception) {
//                    throw IllegalArgumentException("Invalid pinned server certificate", e)
//                }
//            }
//    }

    protected fun run(
        config: ReplicatorConfiguration,
        expectedErrorCode: Int = 0,
        expectedErrorDomain: String? = null,
        reset: Boolean = false,
        onReady: ((Replicator) -> Unit)? = null
    ): Replicator =
        run(testReplicator(config), expectedErrorCode, expectedErrorDomain, reset, onReady)

    protected fun run(
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

        @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
        var ok = false
        try {
            Report.log("Test replicator starting: " + repl.config)
            repl.start(reset)
            listener.awaitCompletion(STD_TIMEOUT_SEC.seconds)
            ok = true
        } finally {
            repl.removeChangeListener(token)
        }

        val err = listener.error
        Report.log("Test replicator finished ${if (ok) "" else "un"}successfully", err)

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
                assertEquals(expectedErrorCode, err.code)
            }
            if (expectedErrorDomain != null) {
                assertEquals(expectedErrorDomain, err.domain)
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

class TestConflictResolver(
    // set this resolver, which will be used while resolving the conflict
    val _resolver: ConflictResolver
) : ConflictResolver {

    var winner: Document? = null

    override fun invoke(conflict: Conflict): Document? {
        winner = _resolver(conflict)
        return winner
    }
}
