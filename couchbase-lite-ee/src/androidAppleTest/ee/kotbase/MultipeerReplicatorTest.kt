/*
 * Copyright 2026 Jeff Lockhart
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

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

// MultipeerReplicatorTest.m
//
// Test Spec: https://github.com/couchbaselabs/couchbase-lite-api/blob/master/spec/tests/T0012-MultipeerReplicator.md
// Version: 1.1.0
//
// Tests that perform actual peer-to-peer networking are @Ignored:
// they require local network access (macOS and iOS prompt for local-network
// permission, and they cannot run on CI VMs or the iOS simulator) and are
// flaky. The upstream iOS test suite disables them as well
// (kDisableMultipeerTests in MultipeerReplicatorTest.m).
class MultipeerReplicatorTest : BaseDbTest() {

    private var identityCount = 0

    private lateinit var otherDatabase: Database

    private val lock = reentrantLock()
    private val replicatorStatuses =
        mutableMapOf<MultipeerReplicator, MutableMap<PeerInfo.PeerId, PeerReplicatorStatus>>()
    private val listenerTokens = mutableMapOf<MultipeerReplicator, MutableList<ListenerToken>>()

    @BeforeTest
    fun setUpMultipeerReplicatorTest() {
        if (!keyStoreAccessAllowed) return
        initPlatformKeyStore()
        cleanupIdentities()
        otherDatabase = createDb("other_db")
    }

    @AfterTest
    fun tearDownMultipeerReplicatorTest() {
        if (::otherDatabase.isInitialized) {
            eraseDb(otherDatabase)
        }
        if (keyStoreAccessAllowed) {
            cleanupIdentities()
        }
    }

    private fun identityName(num: Int): String =
        "$TEST_IDENTITY_COMMON_NAME-$num"

    private fun cleanupIdentities() {
        for (i in 1..TEST_MAX_IDENTITY) {
            try {
                TLSIdentity.deleteIdentity(identityName(i))
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    /**
     * Creates a self-signed identity valid for both client and server authentication.
     *
     * Adaptation from the iOS test suite: the iOS tests create identities signed by a
     * CA issuer from a test asset, using an ObjC-only API. Kotbase uses self-signed
     * identities, which the multipeer replicator accepts equally.
     */
    private fun createIdentity(): TLSIdentity {
        assertTrue(identityCount++ <= TEST_MAX_IDENTITY)
        val alias = identityName(identityCount)
        val cn = "$alias-${Clock.System.now().toEpochMilliseconds()}"
        return TLSIdentity.createIdentity(
            TEST_KEY_USAGES,
            mapOf(TLSIdentity.CERT_ATTRIBUTE_COMMON_NAME to cn),
            null,
            alias
        )
    }

    private fun multipeerReplicator(
        database: Database,
        authenticator: MultipeerCertificateAuthenticator? = null,
        identity: TLSIdentity? = null,
        configure: (MultipeerCollectionConfiguration.() -> Unit)? = null
    ): MultipeerReplicator {
        val collection = database.defaultCollection

        val collConfig = MultipeerCollectionConfiguration(collection)
        configure?.invoke(collConfig)

        val auth = authenticator ?: MultipeerCertificateAuthenticator { peerId, certs ->
            assertNotNull(peerId)
            assertTrue(certs.isNotEmpty())
            true
        }

        val config = MultipeerReplicatorConfiguration(
            TEST_PEER_GROUP_ID,
            identity ?: createIdentity(),
            auth,
            listOf(collConfig)
        )

        return MultipeerReplicator(config)
    }

    private fun addListenerToken(repl: MultipeerReplicator, token: ListenerToken) {
        lock.withLock {
            listenerTokens.getOrPut(repl) { mutableListOf() }.add(token)
        }
    }

    private fun resetListenerTokens(repl: MultipeerReplicator) {
        lock.withLock {
            listenerTokens.remove(repl)
        }?.forEach { it.remove() }
    }

    private fun startMultipeerReplicator(repl: MultipeerReplicator) {
        val active = CountDownLatch(1)
        val token = repl.addStatusListener { status ->
            if (status.transport != null) return@addStatusListener // Ignore per transport status
            assertNull(status.error)
            if (status.isActive) {
                active.countDown()
            }
        }

        addListenerToken(
            repl,
            repl.addPeerReplicatorStatusListener { status ->
                lock.withLock {
                    replicatorStatuses.getOrPut(repl) { mutableMapOf() }[status.peerId] = status
                }
            }
        )

        repl.start()
        runBlocking { assertTrue(active.stdWait()) }
        token.remove()
    }

    private fun stopMultipeerReplicator(repl: MultipeerReplicator) {
        val inactive = CountDownLatch(1)
        val token = repl.addStatusListener { status ->
            if (status.transport != null) return@addStatusListener // Ignore per transport status
            assertNull(status.error)
            if (!status.isActive) {
                inactive.countDown()
            }
        }
        repl.stop()
        runBlocking { assertTrue(inactive.stdWait()) }
        token.remove()

        resetListenerTokens(repl)
    }

    private fun replicatorStatus(repl: MultipeerReplicator, peerId: PeerInfo.PeerId): PeerReplicatorStatus? =
        lock.withLock {
            replicatorStatuses[repl]?.get(peerId)
        }

    private fun waitForReplicatorStatus(
        repl: MultipeerReplicator,
        peerId: PeerInfo.PeerId,
        activityLevel: ReplicatorActivityLevel,
        errorCode: Int = 0
    ) {
        var idleCount = 0
        val deadline = Clock.System.now() + STD_TIMEOUT_SEC.seconds
        while (Clock.System.now() < deadline) {
            val status = replicatorStatus(repl, peerId)
            if (status != null && status.status.activityLevel == activityLevel) {
                when (activityLevel) {
                    ReplicatorActivityLevel.IDLE -> {
                        if (++idleCount >= 3) return // IDLE confirmed
                        runBlocking { delay(0.5.seconds) }
                        continue
                    }
                    ReplicatorActivityLevel.STOPPED -> {
                        if (errorCode != 0) {
                            assertEquals(errorCode, status.status.error?.code)
                        } else {
                            assertNull(status.status.error)
                        }
                        return
                    }
                    else -> return
                }
            }
            idleCount = 0 // Reset if not idle
            runBlocking { delay(0.5.seconds) }
        }
        fail("Timeout waiting for activity level: $activityLevel")
    }

    // Configuration

    /**
     * ### 1. TestConfiguration
     *
     * Test that the configuration can be created successfully
     * and that each property returns the correct value.
     */
    @Test
    fun testConfiguration() {
        if (!keyStoreAccessAllowed) return

        // Create the configuration:
        val identity = createIdentity()

        val auth = MultipeerCertificateAuthenticator { _, _ -> true }

        val collConfig = MultipeerCollectionConfiguration(testDatabase.defaultCollection)

        collConfig.documentIDs = listOf("doc1")

        collConfig.pushFilter = { _, doc, _ -> doc.id == "doc1" }
        collConfig.pullFilter = { _, doc, _ -> doc.id == "doc4" }

        val resolver: MultipeerConflictResolver = { _, conflict -> conflict.remoteDocument }
        collConfig.conflictResolver = resolver

        val config = MultipeerReplicatorConfiguration(
            TEST_PEER_GROUP_ID,
            identity,
            auth,
            listOf(collConfig)
        )

        // Check that each property returns the correct value:
        assertEquals(TEST_PEER_GROUP_ID, config.peerGroupID)
        assertEquals(identity, config.identity)
        assertEquals(auth, config.authenticator)
        assertEquals(setOf(collConfig), config.collections)
        assertEquals(setOf(MultipeerTransport.WIFI), config.transports)

        val configuredCollection = config.collections.first()
        assertEquals(testDatabase.defaultCollection, configuredCollection.collection)
        assertEquals(listOf("doc1"), configuredCollection.documentIDs)
        assertNotNull(configuredCollection.pushFilter)
        assertNotNull(configuredCollection.pullFilter)
        assertEquals(resolver, configuredCollection.conflictResolver)
    }

    /**
     * ### 2. TestConfigurationValidation
     *
     * Validate that a configuration cannot be created if the peerGroupID
     * or collections are invalid.
     */
    @Test
    fun testConfigurationValidation() {
        if (!keyStoreAccessAllowed) return

        val identity = createIdentity()

        val auth = MultipeerCertificateAuthenticator { _, _ -> true }

        val collConfig = MultipeerCollectionConfiguration(testDatabase.defaultCollection)

        // Create the configuration with an empty peerGroupID:
        assertFailsWith<IllegalArgumentException> {
            MultipeerReplicatorConfiguration("", identity, auth, listOf(collConfig))
        }

        // Create the configuration with an empty collections:
        assertFailsWith<IllegalArgumentException> {
            MultipeerReplicatorConfiguration(TEST_PEER_GROUP_ID, identity, auth, emptyList())
        }
    }

    // Stop and Restart

    /**
     * ### 3. TestStartStop
     *
     * Test that a MultipeerReplicator can be started and stopped.
     */
    @Test
    @Ignore
    fun testStartStop() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        val active = CountDownLatch(1)
        val inactive = CountDownLatch(1)

        val token = repl.addStatusListener { status ->
            if (status.transport != null) return@addStatusListener // Ignore per transport status
            assertNull(status.error)
            if (status.isActive) {
                active.countDown()
            } else {
                inactive.countDown()
            }
        }

        repl.start()
        runBlocking { assertTrue(active.stdWait()) }

        repl.stop()
        runBlocking { assertTrue(inactive.stdWait()) }

        token.remove()
    }

    /**
     * ### 4. TestImmediateStartStop
     *
     * Test that a MultipeerReplicator can be started then stopped immediately.
     */
    @Test
    @Ignore
    fun testImmediateStartStop() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        val inactive = CountDownLatch(1)
        val token = repl.addStatusListener { status ->
            if (status.transport != null) return@addStatusListener // Ignore per transport status
            if (!status.isActive) {
                inactive.countDown()
            }
        }

        repl.start()
        repl.stop()

        runBlocking { assertTrue(inactive.stdWait()) }

        token.remove()
    }

    // Database Close

    /**
     * ### 5. TestStopOnCloseDatabase
     *
     * Test that a MultipeerReplicator is stopped when the database is closed.
     */
    @Test
    @Ignore
    fun testStopOnCloseDatabase() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        val active = CountDownLatch(1)
        val inactive = CountDownLatch(1)
        val token = repl.addStatusListener { status ->
            if (status.transport != null) return@addStatusListener // Ignore per transport status
            assertNull(status.error)
            if (status.isActive) {
                active.countDown()
            } else {
                inactive.countDown()
            }
        }

        repl.start()
        runBlocking { assertTrue(active.stdWait()) }

        testDatabase.close()

        runBlocking { assertTrue(inactive.stdWait()) }

        token.remove()
    }

    // Replication

    /**
     * ### 6. TestBasicReplication
     *
     * Test basic replication between two multipeer replicators.
     */
    @Test
    @Ignore
    fun testBasicReplication() {
        if (!keyStoreAccessAllowed) return

        val doc1 = MutableDocument("doc1")
        doc1.setString("greeting", "hello")
        testDatabase.defaultCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("greeting", "hi")
        otherDatabase.defaultCollection.save(doc2)

        val repl1 = multipeerReplicator(testDatabase)
        startMultipeerReplicator(repl1)

        val repl2 = multipeerReplicator(otherDatabase)
        startMultipeerReplicator(repl2)

        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        assertNotNull(testDatabase.defaultCollection.getDocument("doc1"))
        assertNotNull(testDatabase.defaultCollection.getDocument("doc2"))
        assertNotNull(otherDatabase.defaultCollection.getDocument("doc1"))
        assertNotNull(otherDatabase.defaultCollection.getDocument("doc2"))

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)
    }

    // Authenticator

    /**
     * ### 7. TestAuthenticationWithRootCerts
     *
     * Test that the authenticator can authenticate a peer's certificate that is
     * signed by the given root certificates.
     *
     * Adaptation from the iOS test suite: with self-signed identities, the peers'
     * own certificates serve as the trusted root certificates.
     */
    @Test
    @Ignore
    fun testAuthenticateWithRootCerts() {
        if (!keyStoreAccessAllowed) return

        val identity1 = createIdentity()
        val identity2 = createIdentity()

        val auth = MultipeerCertificateAuthenticator(
            listOf(identity1.certs[0], identity2.certs[0])
        )

        // Peer#1
        val repl1 = multipeerReplicator(testDatabase, authenticator = auth, identity = identity1)
        startMultipeerReplicator(repl1)

        // Peer#2
        val repl2 = multipeerReplicator(otherDatabase, authenticator = auth, identity = identity2)
        startMultipeerReplicator(repl2)

        // Wait until the replicator is IDLE
        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)
    }

    /**
     * ### 8. TestAuthenticationWithRootCertsFailed
     *
     * Test that the authenticator can reject a peer's certificate that is not
     * signed by the given root certificates.
     *
     * Adaptation from the iOS test suite: an unrelated self-signed certificate is
     * used as the trusted root certificate, instead of a CA cert from a test asset.
     */
    @Test
    @Ignore
    fun testAuthenticateWithRootCertsFailed() {
        if (!keyStoreAccessAllowed) return

        // A root certificate unrelated to either peer's identity:
        val unrelatedCert = createIdentity().certs[0]
        val auth = MultipeerCertificateAuthenticator(listOf(unrelatedCert))

        // Note: Only the active replicator will have the replicator status error. The passive
        // replicator doesn't get started due to the authentication error. As we don't know in
        // advance which peer will have an active replicator, set up the listener for both.
        val stopped = CountDownLatch(1)

        val listener: PeerReplicatorStatusListener = { status ->
            if (status.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                val error = status.status.error
                assertNotNull(error)
                assertEquals(CBLError.Code.TLS_CLIENT_CERT_REJECTED, error.code)
                assertEquals(CBLError.Domain.CBLITE, error.domain)
                stopped.countDown()
            }
        }

        // Peer#1
        val repl1 = multipeerReplicator(testDatabase, authenticator = auth)
        val token1 = repl1.addPeerReplicatorStatusListener(listener)
        startMultipeerReplicator(repl1)

        // Peer#2
        val repl2 = multipeerReplicator(otherDatabase, authenticator = auth)
        val token2 = repl2.addPeerReplicatorStatusListener(listener)
        startMultipeerReplicator(repl2)

        runBlocking { assertTrue(stopped.stdWait()) }

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)

        token1.remove()
        token2.remove()
    }

    /**
     * ### 9. TestAuthenticationWithCallbackFailed
     *
     * Test that the authenticator can reject via the authentication callback.
     */
    @Test
    @Ignore
    fun testAuthenticateWithCallbackFailed() {
        if (!keyStoreAccessAllowed) return

        val auth = MultipeerCertificateAuthenticator { peerId, certs ->
            assertNotNull(peerId)
            assertTrue(certs.isNotEmpty())
            false
        }

        // Note: Only the active replicator will have the replicator status error. The passive
        // replicator doesn't get started due to the authentication error. As we don't know in
        // advance which peer will have an active replicator, set up the listener for both.
        val stopped = CountDownLatch(1)

        val listener: PeerReplicatorStatusListener = { status ->
            if (status.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                val error = status.status.error
                assertNotNull(error)
                assertEquals(CBLError.Code.TLS_CLIENT_CERT_REJECTED, error.code)
                assertEquals(CBLError.Domain.CBLITE, error.domain)
                stopped.countDown()
            }
        }

        // Peer#1
        val repl1 = multipeerReplicator(testDatabase, authenticator = auth)
        val token1 = repl1.addPeerReplicatorStatusListener(listener)
        startMultipeerReplicator(repl1)

        // Peer#2
        val repl2 = multipeerReplicator(otherDatabase, authenticator = auth)
        val token2 = repl2.addPeerReplicatorStatusListener(listener)
        startMultipeerReplicator(repl2)

        runBlocking { assertTrue(stopped.stdWait()) }

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)

        token1.remove()
        token2.remove()
    }

    // Listener

    /**
     * ### 10. TestPeerDiscoveryListener
     *
     * Test that the peer discovery listener can receive the peer discovery events.
     */
    @Test
    @Ignore
    fun testPeerDiscoveryListener() {
        if (!keyStoreAccessAllowed) return

        val repl1 = multipeerReplicator(testDatabase)
        val repl2 = multipeerReplicator(otherDatabase)

        val peer2Online = CountDownLatch(1)
        val token1 = repl1.addPeerDiscoveryStatusListener { status ->
            assertEquals(repl2.peerId, status.peer)
            if (status.isOnline) {
                peer2Online.countDown()
            }
        }
        startMultipeerReplicator(repl1)

        val peer1Online = CountDownLatch(1)
        val peer1Offline = CountDownLatch(1)
        val token2 = repl2.addPeerDiscoveryStatusListener { status ->
            assertEquals(repl1.peerId, status.peer)
            if (status.isOnline) {
                peer1Online.countDown()
            } else {
                peer1Offline.countDown()
            }
        }
        startMultipeerReplicator(repl2)

        runBlocking {
            assertTrue(peer2Online.stdWait())
            assertTrue(peer1Online.stdWait())
        }

        runBlocking { delay(1.seconds) }

        stopMultipeerReplicator(repl1)

        runBlocking { assertTrue(peer1Offline.stdWait()) }

        stopMultipeerReplicator(repl2)

        token1.remove()
        token2.remove()
    }

    /**
     * ### 11. TestDocumentReplicationListener
     *
     * Test that the document replication listener can receive the document
     * replication events.
     */
    @Test
    @Ignore
    fun testDocumentReplicationListener() {
        if (!keyStoreAccessAllowed) return

        val repl1 = multipeerReplicator(testDatabase)
        val docRepl1 = mutableListOf<PeerDocumentReplication>()
        val token1 = repl1.addPeerDocumentReplicationListener { docRepl ->
            lock.withLock { docRepl1.add(docRepl) }
        }

        val repl2 = multipeerReplicator(otherDatabase)
        val docRepl2 = mutableListOf<PeerDocumentReplication>()
        val token2 = repl2.addPeerDocumentReplicationListener { docRepl ->
            lock.withLock { docRepl2.add(docRepl) }
        }

        startMultipeerReplicator(repl1)
        startMultipeerReplicator(repl2)

        // Save a doc in one peer:
        val doc1 = MutableDocument("doc1")
        doc1.setString("greeting", "hello")
        testDatabase.defaultCollection.save(doc1)

        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        lock.withLock {
            assertEquals(1, docRepl1.size)
            assertTrue(docRepl1[0].isPush)
            assertEquals(1, docRepl1[0].documents.size)
            with(docRepl1[0].documents[0]) {
                assertEquals("doc1", id)
                assertTrue(flags.isEmpty())
                assertNull(error)
                assertEquals("_default", scope)
                assertEquals("_default", collection)
            }

            assertEquals(1, docRepl2.size)
            assertFalse(docRepl2[0].isPush)
            assertEquals(1, docRepl2[0].documents.size)
            with(docRepl2[0].documents[0]) {
                assertEquals("doc1", id)
                assertTrue(flags.isEmpty())
                assertNull(error)
                assertEquals("_default", scope)
                assertEquals("_default", collection)
            }
        }

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)

        token1.remove()
        token2.remove()
    }

    // Conflict Resolver

    private fun runConflictResolverTest(
        resolver: MultipeerConflictResolver?,
        verify: (Document?) -> Boolean
    ) {
        // Peer#1
        var repl1 = multipeerReplicator(testDatabase) {
            conflictResolver = resolver
        }
        startMultipeerReplicator(repl1)

        // Peer#2
        var repl2 = multipeerReplicator(otherDatabase) {
            conflictResolver = resolver
        }
        startMultipeerReplicator(repl2)

        // Save a doc in peer #1:
        val doc = MutableDocument("doc1")
        doc.setString("greeting", "hello")
        testDatabase.defaultCollection.save(doc)

        // Wait until the replicator is IDLE
        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        // Stops:
        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)

        // Update the doc in both peers, saving on the otherDB twice:
        val doc1 = testDatabase.defaultCollection.getDocument("doc1")!!.toMutable()
        doc1.setString("greeting", "hi")
        testDatabase.defaultCollection.save(doc1)

        val doc2 = otherDatabase.defaultCollection.getDocument("doc1")!!.toMutable()
        doc2.setString("greeting", "hey")
        otherDatabase.defaultCollection.save(doc2)

        doc2.setString("greeting", "howdy")
        otherDatabase.defaultCollection.save(doc2)

        // Restart both peers:
        repl1 = multipeerReplicator(testDatabase) {
            conflictResolver = resolver
        }
        startMultipeerReplicator(repl1)

        repl2 = multipeerReplicator(otherDatabase) {
            conflictResolver = resolver
        }
        startMultipeerReplicator(repl2)

        // Wait until the replicator is IDLE
        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        // Check the doc on both peers:
        assertTrue(verify(testDatabase.defaultCollection.getDocument("doc1")))
        assertTrue(verify(otherDatabase.defaultCollection.getDocument("doc1")))

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)
    }

    /**
     * ### 12. TestDefaultConflictResolver
     *
     * Test that the default conflict resolver is used when no custom conflict
     * resolver is specified.
     */
    @Test
    @Ignore
    fun testDefaultConflictResolver() {
        if (!keyStoreAccessAllowed) return

        runConflictResolverTest(null) { resolvedDoc ->
            resolvedDoc?.getString("greeting") == "howdy"
        }
    }

    /**
     * ### 13. TestCustomConflictResolver
     *
     * Test that the custom conflict resolver is used when it is specified.
     */
    @Test
    @Ignore
    fun testCustomConflictResolver() {
        if (!keyStoreAccessAllowed) return

        val resolver: MultipeerConflictResolver = { _, conflict ->
            val greeting = conflict.localDocument?.getString("greeting")
            if (greeting == "hi") conflict.localDocument else conflict.remoteDocument
        }

        runConflictResolverTest(resolver) { resolvedDoc ->
            resolvedDoc?.getString("greeting") == "hi"
        }
    }

    // Replication Filter

    /**
     * ### 14. TestDocumentIDsFilter
     *
     * Test that the documentIDs filter is used when it's specified.
     */
    @Test
    @Ignore
    fun testDocumentIDsFilter() {
        if (!keyStoreAccessAllowed) return

        // Peer#1
        val repl1 = multipeerReplicator(testDatabase) {
            documentIDs = listOf("doc1", "doc3")
        }
        startMultipeerReplicator(repl1)

        // Peer#2
        val repl2 = multipeerReplicator(otherDatabase)
        startMultipeerReplicator(repl2)

        // Save 3 docs in peer #1:
        val doc1 = MutableDocument("doc1")
        doc1.setString("greeting", "hello")
        testDatabase.defaultCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("greeting", "hi")
        testDatabase.defaultCollection.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("greeting", "howdy")
        testDatabase.defaultCollection.save(doc3)

        // Wait until the replicator is IDLE
        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        // Check that only doc1 and doc3 are pushed to Peer #2
        val checkDoc1 = otherDatabase.defaultCollection.getDocument("doc1")
        assertNotNull(checkDoc1)
        assertEquals("hello", checkDoc1.getString("greeting"))

        assertNull(otherDatabase.defaultCollection.getDocument("doc2"))

        val checkDoc3 = otherDatabase.defaultCollection.getDocument("doc3")
        assertNotNull(checkDoc3)
        assertEquals("howdy", checkDoc3.getString("greeting"))

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)
    }

    /**
     * ### 15. TestPushPullFilter
     *
     * Test that the push and pull filters are used when they are specified.
     */
    @Test
    @Ignore
    fun testPushPullFilter() {
        if (!keyStoreAccessAllowed) return

        // Peer#1
        val repl1 = multipeerReplicator(testDatabase) {
            pushFilter = { _, doc, _ -> doc.id == "doc1" }
            pullFilter = { _, doc, _ -> doc.id == "doc4" }
        }
        startMultipeerReplicator(repl1)

        // Peer#2
        val repl2 = multipeerReplicator(otherDatabase)
        startMultipeerReplicator(repl2)

        // Save doc1 and doc2 in peer #1:
        val doc1 = MutableDocument("doc1")
        doc1.setString("greeting", "hello")
        testDatabase.defaultCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("greeting", "hi")
        testDatabase.defaultCollection.save(doc2)

        // Save doc3 and doc4 in peer #2:
        val doc3 = MutableDocument("doc3")
        doc3.setString("greeting", "howdy")
        otherDatabase.defaultCollection.save(doc3)

        val doc4 = MutableDocument("doc4")
        doc4.setString("greeting", "hey")
        otherDatabase.defaultCollection.save(doc4)

        // Wait until the replicator is IDLE:
        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        // Check that only doc4 but not doc3 are pulled to peer #1:
        val checkDoc4 = testDatabase.defaultCollection.getDocument("doc4")
        assertNotNull(checkDoc4)
        assertEquals("hey", checkDoc4.getString("greeting"))

        assertNull(testDatabase.defaultCollection.getDocument("doc3"))

        // Check that only doc1 but not doc2 are pushed to peer #2:
        val checkDoc1 = otherDatabase.defaultCollection.getDocument("doc1")
        assertNotNull(checkDoc1)
        assertEquals("hello", checkDoc1.getString("greeting"))

        assertNull(otherDatabase.defaultCollection.getDocument("doc2"))

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)
    }

    // Peer Info

    /**
     * ### 16. TestPeerID
     *
     * Test that getting the MultipeerReplicator's peerID returns a value as expected.
     */
    @Test
    @Ignore
    fun testPeerID() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)
        val peerId = repl.peerId
        assertEquals(PeerInfo.PEER_ID_LENGTH, peerId.bytes.size)

        startMultipeerReplicator(repl)
        assertContentEquals(peerId.bytes, repl.peerId.bytes)

        stopMultipeerReplicator(repl)
        assertContentEquals(peerId.bytes, repl.peerId.bytes)
    }

    /**
     * ### 17. TestNeighborPeers
     *
     * Test that getting the MultipeerReplicator's neighborPeers returns the
     * online peers as expected.
     */
    @Test
    @Ignore
    fun testNeighborPeers() {
        if (!keyStoreAccessAllowed) return

        val repl1 = multipeerReplicator(testDatabase)
        assertTrue(repl1.neighborPeers.isEmpty())

        startMultipeerReplicator(repl1)
        assertTrue(repl1.neighborPeers.isEmpty())

        val repl2 = multipeerReplicator(otherDatabase)
        assertTrue(repl2.neighborPeers.isEmpty())

        startMultipeerReplicator(repl2)

        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        assertEquals(setOf(repl2.peerId), repl1.neighborPeers)
        assertEquals(setOf(repl1.peerId), repl2.neighborPeers)

        stopMultipeerReplicator(repl1)
        assertTrue(repl1.neighborPeers.isEmpty())

        // Wait to ensure the neighborPeers are updated
        runBlocking { delay(5.seconds) }
        assertTrue(repl2.neighborPeers.isEmpty())

        stopMultipeerReplicator(repl2)
        assertTrue(repl2.neighborPeers.isEmpty())
    }

    /**
     * ### 18. TestPeerInfo
     *
     * Verify that retrieving peer info returns the expected PeerInfo object for
     * known peers. If the peer is unknown, ensure that null is returned as expected.
     */
    @Test
    @Ignore
    fun testPeerInfo() {
        if (!keyStoreAccessAllowed) return

        val repl1 = multipeerReplicator(testDatabase)
        val repl2 = multipeerReplicator(otherDatabase)

        var repl1Peer1 = repl1.getPeerInfo(repl1.peerId)
        assertNotNull(repl1Peer1)
        assertEquals(repl1.peerId, repl1Peer1.peerId)
        assertNotNull(repl1Peer1.certificate)
        assertTrue(repl1Peer1.neighbors.isEmpty())
        assertNull(repl1Peer1.replicatorTransport)
        assertTrue(repl1Peer1.transports.isEmpty())
        assertEquals(ReplicatorActivityLevel.STOPPED, repl1Peer1.replicatorStatus.activityLevel)

        assertNull(repl1.getPeerInfo(repl2.peerId))

        startMultipeerReplicator(repl1)
        startMultipeerReplicator(repl2)

        waitForReplicatorStatus(repl1, repl2.peerId, ReplicatorActivityLevel.IDLE)
        waitForReplicatorStatus(repl2, repl1.peerId, ReplicatorActivityLevel.IDLE)

        repl1Peer1 = repl1.getPeerInfo(repl1.peerId)
        assertNotNull(repl1Peer1)
        assertEquals(setOf(repl2.peerId), repl1Peer1.neighbors)
        assertNull(repl1Peer1.replicatorTransport)                                          // No replication to itself
        assertTrue(repl1Peer1.transports.isEmpty())                                         // No replication to itself
        assertEquals(ReplicatorActivityLevel.STOPPED, repl1Peer1.replicatorStatus.activityLevel) // No replication to itself

        val repl1Peer2 = repl1.getPeerInfo(repl2.peerId)
        assertNotNull(repl1Peer2)
        assertEquals(setOf(repl1.peerId), repl1Peer2.neighbors)
        assertEquals(MultipeerTransport.WIFI, repl1Peer2.replicatorTransport)
        assertEquals(setOf(MultipeerTransport.WIFI), repl1Peer2.transports)
        assertEquals(ReplicatorActivityLevel.IDLE, repl1Peer2.replicatorStatus.activityLevel)

        stopMultipeerReplicator(repl1)
        stopMultipeerReplicator(repl2)
    }

    // Transport Status

    /**
     * ### 19. TestGetTransportStatus
     *
     * Test that getting status returns the correct status for the transport.
     */
    @Test
    @Ignore
    fun testGetTransportStatus() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        var status = repl.getStatus(MultipeerTransport.WIFI)
        assertNotNull(status)
        assertFalse(status.isActive)
        assertNull(status.error)

        startMultipeerReplicator(repl)

        status = repl.getStatus(MultipeerTransport.WIFI)
        assertNotNull(status)
        assertTrue(status.isActive)
        assertNull(status.error)
        assertEquals(MultipeerTransport.WIFI, status.transport)

        stopMultipeerReplicator(repl)

        status = repl.getStatus(MultipeerTransport.WIFI)
        assertNotNull(status)
        assertFalse(status.isActive)
        assertNull(status.error)
    }

    /**
     * ### 20. TestGetTransportStatusNotConfigured
     *
     * Test that getting status returns null for a transport that is not configured.
     */
    @Test
    @Ignore
    fun testGetTransportStatusNotConfigured() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        startMultipeerReplicator(repl)

        assertNull(repl.getStatus(MultipeerTransport.BLUETOOTH))

        stopMultipeerReplicator(repl)
    }

    /**
     * ### 21. TestGetTransportStatusBeforeStart
     *
     * Test that getting status returns inactive status when the replicator
     * has not been started.
     */
    @Test
    fun testGetTransportStatusBeforeStart() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        val status = repl.getStatus(MultipeerTransport.WIFI)
        assertNotNull(status)
        assertFalse(status.isActive)
        assertNull(status.error)
    }

    /**
     * ### 22. TestTransportSpecificStatusListener
     *
     * Test that the status listener receives both transport-specific and aggregate
     * status events when the replicator starts and stops.
     */
    @Test
    @Ignore
    fun testTransportSpecificStatusListener() {
        if (!keyStoreAccessAllowed) return

        val repl = multipeerReplicator(testDatabase)

        val wifiActive = CountDownLatch(1)
        val aggregateActive = CountDownLatch(1)
        val wifiInactive = CountDownLatch(1)
        val aggregateInactive = CountDownLatch(1)

        val token = repl.addStatusListener { status ->
            when (status.transport) {
                null -> {
                    assertNull(status.error)
                    if (status.isActive) aggregateActive.countDown()
                    else aggregateInactive.countDown()
                }
                MultipeerTransport.WIFI -> {
                    if (status.isActive) wifiActive.countDown()
                    else wifiInactive.countDown()
                }
                else -> {}
            }
        }

        repl.start()
        runBlocking {
            assertTrue(wifiActive.stdWait())
            assertTrue(aggregateActive.stdWait())
        }

        repl.stop()
        runBlocking {
            assertTrue(wifiInactive.stdWait())
            assertTrue(aggregateInactive.stdWait())
        }

        token.remove()
    }

    companion object {
        private const val TEST_PEER_GROUP_ID = "CBLMultipeerReplTestGroup"

        private const val TEST_MAX_IDENTITY = 10
        private const val TEST_IDENTITY_COMMON_NAME = "cbl-multipeer-repl-test"

        private val TEST_KEY_USAGES = setOf(KeyUsage.CLIENT_AUTH, KeyUsage.SERVER_AUTH)
    }
}
