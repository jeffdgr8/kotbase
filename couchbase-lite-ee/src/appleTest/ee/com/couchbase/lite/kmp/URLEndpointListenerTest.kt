package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.*
import com.couchbase.lite.kmp.internal.utils.PlatformUtils
import com.couchbase.lite.kmp.internal.utils.TestUtils.assertThrowsCBL
import com.soywiz.kmem.Platform
import com.udobny.kmp.ext.toByteArray
import com.udobny.kmp.ext.toSecCertificate
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import okio.buffer
import okio.use
import platform.CoreFoundation.CFStringRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSUserDefaults
import platform.Security.*
import platform.posix.EADDRINUSE
import platform.posix.ECONNREFUSED
import kotlin.math.max
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

open class URLEndpointListenerBaseTest : BaseReplicatorTest() {

    val wsPort = 4984
    val wssPort = 4985
    val serverCertLabel = "CBL-Server-Cert"
    val clientCertLabel = "CBL-Client-Cert"

    var listener: URLEndpointListener? = null

    val isHostApp: Boolean
        get() {
            return if (Platform.os.isIos) {
                val defaults = NSUserDefaults.standardUserDefaults
                defaults.boolForKey("hostApp")
            } else {
                true
            }
        }

    val keyChainAccessAllowed: Boolean
        get() {
            return if (Platform.os.isIos) {
                isHostApp
            } else {
                true
            }
        }

    // Helper methods
    // Listener Helper methods

    protected fun startListener(
        tls: Boolean = true,
        auth: ListenerAuthenticator? = null
    ): URLEndpointListener {
        // Stop:
        listener?.stop()

        // Listener:
        val config = URLEndpointListenerConfiguration(otherDB)
        config.port = if (tls) wssPort else wsPort
        config.isTlsDisabled = !tls
        config.authenticator = auth

        return startListener(config)
    }

    protected fun startListener(config: URLEndpointListenerConfiguration): URLEndpointListener {
        listener = URLEndpointListener(config)

        // Start:
        listener!!.start()

        return listener!!
    }

    protected fun stopListener(listener: URLEndpointListener? = null) {
        val l = listener ?: this.listener

        l?.stop()
        // TODO:
        //l?.tlsIdentity?.deleteFromKeyChain()
    }

    private fun cleanUpIdentities() {
        try {
            // TODO:
            //URLEndpointListener.deleteAnonymousIdentities()
        } catch (e: Exception) {
            //ignore
        }
    }

    // TLS Identity helpers

    fun createTLSIdentity(isServer: Boolean = true): TLSIdentity? {
        if (!keyChainAccessAllowed) return null

        val label = if (isServer) serverCertLabel else clientCertLabel

        // cleanup client cert authenticator identity
        TLSIdentity.deleteIdentity(label)

        // Create client identity:
        val attrs = mapOf(kCBLCertAttrCommonName to if (isServer) "CBL-Server" else "daniel")
        return TLSIdentity.createIdentity(false, attrs, null, label)
    }

    protected fun checkCertificateEqual(cert1: SecCertificateRef, cert2: SecCertificateRef) {
        memScoped {
            val cn1 = alloc<CFStringRefVar>()
            assertEquals(errSecSuccess, SecCertificateCopyCommonName(cert1, cn1.ptr))

            val cn2 = alloc<CFStringRefVar>()
            assertEquals(errSecSuccess, SecCertificateCopyCommonName(cert2, cn2.ptr))

            assertEquals(cn1.value!!, cn2.value!!)
        }
    }

    // Replicator helper methods

    /// - Note: default value for continuous is true! Thats is common in this test suite
    protected fun createReplicator(
        db: Database,
        target: Endpoint,
        continuous: Boolean = true,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        serverCert: ByteArray? = null
    ): Replicator {
        val config = ReplicatorConfiguration(db, target)
        config.type = type
        config.isContinuous = continuous
        config.pinnedServerCertificate = serverCert
        return Replicator(config)
    }

    protected fun run(
        target: Endpoint,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        continuous: Boolean = false,
        auth: Authenticator? = null,
        serverCert: ByteArray? = null,
        expectedError: Int = 0
    ) {
        val config = makeConfig(target, type, continuous).apply {
            authenticator = auth
            pinnedServerCertificate = serverCert
        }
        run(config, expectedError)
    }

    protected fun run(
        target: Endpoint,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        continuous: Boolean = false,
        auth: Authenticator? = null,
        acceptSelfSignedOnly: Boolean = false,
        serverCert: ByteArray? = null,
        maxAttempts: Int = 0,
        expectedError: Int = 0
    ) {
        val config = makeConfig(
            target,
            type,
            continuous,
            auth,
            acceptSelfSignedOnly,
            serverCert,
            maxAttempts
        )
        run(config, expectedError)
    }

    protected fun makeConfig(
        target: Endpoint,
        type: ReplicatorType = ReplicatorType.PUSH_AND_PULL,
        continuous: Boolean = false,
        auth: Authenticator? = null,
        acceptSelfSignedOnly: Boolean = false,
        serverCert: ByteArray? = null,
        maxAttempts: Int = 0
    ): ReplicatorConfiguration {
        return makeConfig(target, type, continuous).apply {
            authenticator = auth
            pinnedServerCertificate = serverCert
            this.maxAttempts = maxAttempts
            isAcceptOnlySelfSignedServerCertificate = acceptSelfSignedOnly
        }
    }

    @BeforeTest
    fun setUp() {
        cleanUpIdentities()
    }

    @AfterTest
    fun tearDown() {
        stopListener()
        cleanUpIdentities()
    }
}

class URLEndpointListenerTest : URLEndpointListenerBaseTest() {

    // Reusable helper methods

    /// Two replicators, replicates docs to the this.listener;
    /// pushAndPull
    private fun validateMultipleReplicationsTo(
        listener: URLEndpointListener,
        type: ReplicatorType
    ) = runBlocking {
        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)
        val count = listener.config.database.count

        // open DBs
        Database.delete("db1")
        Database.delete("db2")
        val db1 = Database("db1")
        val db2 = Database("db2")

        // For keeping the replication long enough to validate connection status, we will use blob
        val imageData = PlatformUtils.getAsset("image.jpg")!!.use { input ->
            input.buffer().readByteArray()
        }

        // DB#1
        val doc1 = MutableDocument()
        val blob1 = Blob("image/jpg", imageData)
        doc1.setBlob("blob", blob1)
        db1.save(doc1)

        // DB#2
        val doc2 = MutableDocument()
        val blob2 = Blob("image/jpg", imageData)
        doc2.setBlob("blob", blob2)
        db2.save(doc2)

        val repl1 = createReplicator(
            db1,
            listener.localURLEndpoint,
            false,
            type,
            listener.tlsIdentity!!.certs[0].toByteArray()
        )
        val repl2 = createReplicator(
            db2,
            listener.localURLEndpoint,
            false,
            type,
            listener.tlsIdentity?.certs?.get(0)?.toByteArray()
        )
        val changeListener = { change: ReplicatorChange ->
            if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                if (change.replicator.config.database.name == "db1") {
                    mutex1.unlock()
                } else {
                    mutex2.unlock()
                }
            }
        }
        val token1 = repl1.addChangeListener(changeListener)
        val token2 = repl2.addChangeListener(changeListener)

        repl1.start()
        repl2.start()
        withTimeout(5.seconds) {
            mutex1.lock()
            mutex2.lock()
        }

        // pushAndPull might cause race, so only checking push
        if (type == ReplicatorType.PUSH) {
            assertEquals(count + 2, listener.config.database.count)
        }

        // pushAndPull might cause race, so only checking pull
        if (type == ReplicatorType.PULL) {
            assertEquals(count + 1, db1.count) // existing docs + pulls one doc from db#2
            assertEquals(count + 1, db2.count) // existing docs + pulls one doc from db#1
        }

        repl1.removeChangeListener(token1)
        repl2.removeChangeListener(token2)

        db1.close()
        db2.close()
    }

    private fun validateActiveReplicationsAndURLEndpointListener(
        isDeleteDBs: Boolean
    ) = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val idleMutex1 = Mutex(true)
        val idleMutex2 = Mutex(true)
        val stopMutex1 = Mutex(true)
        val stopMutex2 = Mutex(true)

        val doc1 = MutableDocument("db-doc")
        baseTestDb.save(doc1)
        val doc2 = MutableDocument("other-db-doc")
        otherDB.save(doc2)

        // start listener
        startListener()

        // replicator#1
        val repl1 = createReplicator(otherDB, DatabaseEndpoint(baseTestDb))

        // replicator#2
        Database.delete("db2")
        val db2 = Database("db2")
        val repl2 = createReplicator(
            db2,
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray()
        )

        val changeListener = { change: ReplicatorChange ->
            if (change.status.activityLevel == ReplicatorActivityLevel.IDLE
                && change.status.progress.completed == change.status.progress.total) {

                if (change.replicator.config.database.name == "db2") {
                    idleMutex2.unlock()
                } else {
                    idleMutex1.unlock()
                }
            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                if (change.replicator.config.database.name == "db2") {
                    stopMutex2.unlock()
                } else {
                    stopMutex1.unlock()
                }
            }
        }
        val token1 = repl1.addChangeListener(changeListener)
        val token2 = repl2.addChangeListener(changeListener)
        repl1.start()
        repl2.start()
        withTimeout(10.seconds) { // TODO: FIXME
            idleMutex1.lock()
            idleMutex2.lock()
        }

        if (isDeleteDBs) {
            db2.delete()
            otherDB.delete()
        } else {
            db2.close()
            otherDB.close()
        }

        withTimeout(10.seconds) { // TODO: FIXME
            stopMutex1.lock()
            stopMutex2.lock()
        }
        repl1.removeChangeListener(token1)
        repl2.removeChangeListener(token2)
        stopListener()
    }

    private fun validateActiveReplicatorAndURLEndpointListeners(isDeleteDB: Boolean) = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val idleMutex = Mutex(true)
        val stopMutex = Mutex(true)

        val config = URLEndpointListenerConfiguration(otherDB)
        val listener1 = URLEndpointListener(config)
        val listener2 = URLEndpointListener(config)

        // listener
        listener1.start()
        listener2.start()

        val doc1 = MutableDocument("db-doc")
        baseTestDb.save(doc1)
        val doc2 = MutableDocument("other-db-doc")
        otherDB.save(doc2)

        // replicator
        val repl1 = createReplicator(
            otherDB,
            listener1.localURLEndpoint,
            serverCert = listener1.tlsIdentity?.certs?.get(0)?.toByteArray()
        )
        val token1 = repl1.addChangeListener { change ->
            if (change.status.activityLevel == ReplicatorActivityLevel.IDLE
                && change.status.progress.completed == change.status.progress.total) {

                idleMutex.unlock()

            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                stopMutex.unlock()
            }
        }
        repl1.start()
        withTimeout(5.seconds) {
            idleMutex.lock()
        }

        if (isDeleteDB) {
            otherDB.delete()
        } else {
            otherDB.close()
        }

        withTimeout(5.seconds) {
            stopMutex.lock()
        }

        // cleanup
        repl1.removeChangeListener(token1)
        stopListener(listener1)
        stopListener(listener2)
    }

    // -- Tests

    @Test
    fun testPort() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(otherDB)
        config.port = wsPort
        listener = URLEndpointListener(config)
        assertNull(listener!!.port)

        // Start:
        listener!!.start()
        assertEquals(wsPort, listener!!.port)

        stopListener()
        assertNull(listener!!.port)
    }

    @Test
    fun testEmptyPort() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(otherDB)
        listener = URLEndpointListener(config)
        assertNull(listener!!.port)

        // Start:
        listener!!.start()
        assertNotEquals(0, listener!!.port)

        stopListener()
        assertNull(listener!!.port)
    }

    @Test
    fun testBusyPort() {
        if (!keyChainAccessAllowed) return

        startListener()

        val config = URLEndpointListenerConfiguration(otherDB)
        config.port = listener!!.port
        val listener2 = URLEndpointListener(config)

        assertThrowsCBL(CBLError.Domain.POSIX, EADDRINUSE) {
            listener2.start()
        }
    }

    @Test
    fun testURLs() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(otherDB)
        config.port = wsPort
        listener = URLEndpointListener(config)
        assertTrue(listener!!.urls.isEmpty())

        // Start:
        listener!!.start()
        assertTrue(listener!!.urls.isNotEmpty())

        stopListener()
        assertTrue(listener!!.urls.isEmpty())
    }

    @Test
    fun testTLSListenerAnonymousIdentity() {
        if (!keyChainAccessAllowed) return

        val doc = MutableDocument("doc-1")
        otherDB.save(doc)

        val config = URLEndpointListenerConfiguration(otherDB)
        val listener = URLEndpointListener(config)
        assertNull(listener.tlsIdentity)
        listener.start()
        assertNotNull(listener.tlsIdentity)

        // anonymous identity
        run(
            listener.localURLEndpoint,
            serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()
        )

        // Different pinned cert
        TLSIdentity.deleteIdentity("dummy")
        val tlsID = TLSIdentity.createIdentity(
            false,
            mapOf(kCBLCertAttrCommonName to "client"),
            null,
            "dummy"
        )
        run(
            listener.localURLEndpoint,
            serverCert = tlsID.certs[0].toByteArray(),
            expectedError = CBLErrorTLSCertUnknownRoot.toInt()
        )
        TLSIdentity.deleteIdentity("dummy")

        // No pinned cert
        run(listener.localURLEndpoint, expectedError = CBLErrorTLSCertUnknownRoot.toInt())

        stopListener(listener)
        assertNull(listener.tlsIdentity)
        TLSIdentity.deleteIdentity(serverCertLabel)
    }

    @Test
    fun testTLSListenerUserIdentity() {
        if (!keyChainAccessAllowed) return

        val doc = MutableDocument("doc-1")
        otherDB.save(doc)

        val tls = createTLSIdentity()
        val config = URLEndpointListenerConfiguration(otherDB)
        config.tlsIdentity = tls
        val listener = URLEndpointListener(config)
        assertNull(listener.tlsIdentity)
        listener.start()
        assertNotNull(listener.tlsIdentity)

        run(
            listener.localURLEndpoint,
            serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()
        )

        // Different pinned cert
        TLSIdentity.deleteIdentity("dummy")
        val tlsID = TLSIdentity.createIdentity(
            false,
            mapOf(kCBLCertAttrCommonName to "client"),
            null,
            "dummy"
        )
        run(
            listener.localURLEndpoint,
            serverCert = tlsID.certs[0].toByteArray(),
            expectedError = CBLErrorTLSCertUnknownRoot.toInt()
        )
        TLSIdentity.deleteIdentity("dummy")

        // No pinned cert
        run(listener.localURLEndpoint, expectedError = CBLErrorTLSCertUnknownRoot.toInt())

        stopListener(listener)
        assertNull(listener.tlsIdentity)
        TLSIdentity.deleteIdentity(serverCertLabel)
    }

    @Test
    fun testNonTLSNullListenerAuthenticator() {
        if (!keyChainAccessAllowed) return

        val listener = startListener(false)
        assertNull(listener.tlsIdentity)

        // Replicator - No Authenticator:
        run(listener.localURLEndpoint)

        // Replicator - Basic Authenticator:
        val auth = BasicAuthenticator("daniel", "123".toCharArray())
        run(listener.localURLEndpoint, auth = auth)

        // Replicator - Client Cert Authenticator
        val certAuth = ClientCertificateAuthenticator(createTLSIdentity(false)!!)
        run(listener.localURLEndpoint, auth = certAuth)
        TLSIdentity.deleteIdentity(clientCertLabel)

        // Cleanup:
        stopListener()
    }

    @Test
    fun testNonTLSPasswordListenerAuthenticator() {
        if (!keyChainAccessAllowed) return

        // Listener:
        val listenerAuth = ListenerPasswordAuthenticator { username, password ->
            username == "daniel" && password.concatToString() == "123"
        }
        val listener = startListener(false, listenerAuth)

        // Replicator - No Authenticator:
        run(listener.localURLEndpoint, expectedError = CBLErrorHTTPAuthRequired.toInt())

        // Replicator - Wrong Username:
        var auth = BasicAuthenticator("daneil", "123".toCharArray())
        run(
            listener.localURLEndpoint,
            auth = auth,
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Wrong Password:
        auth = BasicAuthenticator("daniel", "456".toCharArray())
        run(
            listener.localURLEndpoint,
            auth = auth,
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Client Cert Authenticator
        val certAuth = ClientCertificateAuthenticator(createTLSIdentity(false)!!)
        run(
            listener.localURLEndpoint,
            auth = certAuth,
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )
        TLSIdentity.deleteIdentity(clientCertLabel)

        // Replicator - Success:
        auth = BasicAuthenticator("daniel", "123".toCharArray())
        run(listener.localURLEndpoint, auth = auth)

        // Cleanup:
        stopListener()
    }

    @Test
    fun testClientCertAuthWithCallback() {
        if (!keyChainAccessAllowed) return

        // Listener:
        val listenerAuth = ListenerCertificateAuthenticator { certs ->
            assertEquals(1, certs.size)
            memScoped {
                val commonName = alloc<CFStringRefVar>()
                val cert = certs[0].toSecCertificate()
                val status = SecCertificateCopyCommonName(cert, commonName.ptr)
                assertEquals(errSecSuccess, status)
                assertNotNull(commonName.value)
                val commonNameString = CFBridgingRelease(commonName.value) as String
                assertEquals("daniel", commonNameString)
            }
            true
        }
        val listener = startListener(auth = listenerAuth)
        assertNotNull(listener.tlsIdentity)
        assertEquals(1, listener.tlsIdentity!!.certs.size)

        // Replicator:
        val auth = ClientCertificateAuthenticator(createTLSIdentity(false)!!)
        val serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()
        run(listener.localURLEndpoint, auth = auth, serverCert = serverCert)

        // Cleanup:
        TLSIdentity.deleteIdentity(clientCertLabel)
        stopListener()
    }

    @Test
    fun testClientCertAuthWithCallbackError() {
        if (!keyChainAccessAllowed) return

        // Listener:
        val listenerAuth = ListenerCertificateAuthenticator { certs ->
            assertEquals(1, certs.size)
            false
        }
        val listener = startListener(auth = listenerAuth)
        assertNotNull(listener.tlsIdentity)
        assertEquals(1, listener.tlsIdentity!!.certs.size)

        // Replicator:
        val auth = ClientCertificateAuthenticator(createTLSIdentity(false)!!)
        val serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()
        run(
            listener.localURLEndpoint,
            auth = auth,
            serverCert = serverCert,
            expectedError = CBLErrorTLSClientCertRejected.toInt()
        )

        // Cleanup:
        TLSIdentity.deleteIdentity(clientCertLabel)
        stopListener()
    }

    @Test
    fun testClientCertAuthWithRootCerts() {
        if (!keyChainAccessAllowed) return

        // Root Cert:
        val rootCert = PlatformUtils.getAsset("identity/client-ca.der")!!.use { input ->
            input.buffer().readByteArray()
        }

        // Listener:
        val listenerAuth = ListenerCertificateAuthenticator(listOf(rootCert))
        val listener = startListener(auth = listenerAuth)

        // Cleanup:
        TLSIdentity.deleteIdentity(clientCertLabel)

        // Create client identity:
        val clientCertData = PlatformUtils.getAsset("identity/client.p12")!!.use { input ->
            input.buffer().readByteArray()
        }
        val identity = TLSIdentity.importIdentity(clientCertData, "123".toCharArray(), clientCertLabel)

        // Replicator:
        val auth = ClientCertificateAuthenticator(identity)
        val serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()

        try {
            run(listener.localURLEndpoint, auth = auth, serverCert = serverCert)
        } catch (e: Exception) {
            // ignore
        }

        // Cleanup:
        TLSIdentity.deleteIdentity(clientCertLabel)
        stopListener()
    }

    @Test
    fun testClientCertAuthWithRootCertsError() {
        if (!keyChainAccessAllowed) return

        // Root Cert:
        val rootCert = PlatformUtils.getAsset("identity/client-ca.der")!!.use { input ->
            input.buffer().readByteArray()
        }

        // Listener:
        val listenerAuth = ListenerCertificateAuthenticator(listOf(rootCert))
        val listener = startListener(auth = listenerAuth)

        // Replicator:
        val auth = ClientCertificateAuthenticator(createTLSIdentity(false)!!)
        val serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()

        try {
            run(
                listener.localURLEndpoint,
                auth = auth,
                serverCert = serverCert,
                expectedError = CBLErrorTLSClientCertRejected.toInt()
            )
        } catch (e: Exception) {
            // ignore
        }

        // Cleanup:
        TLSIdentity.deleteIdentity(clientCertLabel)
        stopListener()
    }

    @Test
    fun testConnectionStatus() = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val replicatorStop = Mutex(true)
        val pullFilterBusy = Mutex(true)
        val config = URLEndpointListenerConfiguration(otherDB)
        config.port = wsPort
        config.isTlsDisabled = true
        listener = URLEndpointListener(config)
        assertEquals(0, listener!!.status!!.connectionCount)
        assertEquals(0, listener!!.status!!.activeConnectionCount)

        // Start:
        listener!!.start()
        assertEquals(0, listener!!.status!!.connectionCount)
        assertEquals(0, listener!!.status!!.activeConnectionCount)

        val doc1 = MutableDocument()
        otherDB.save(doc1)

        var maxConnectionCount = 0
        var maxActiveCount = 0
        val rConfig = ReplicatorConfiguration(baseTestDb, listener!!.localURLEndpoint)
        rConfig.type = ReplicatorType.PULL
        rConfig.isContinuous = false
        rConfig.pullFilter = { doc, flags ->
            val s = listener!!.status!!
            maxConnectionCount = max(s.connectionCount, maxConnectionCount)
            maxActiveCount = max(s.activeConnectionCount, maxActiveCount)
            pullFilterBusy.unlock()
            true
        }

        val repl = Replicator(rConfig)
        val token = repl.addChangeListener { change ->
            if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                replicatorStop.unlock()
            }
        }

        repl.start()
        withTimeout(5.seconds) {
            pullFilterBusy.lock()
            replicatorStop.lock()
        }
        repl.removeChangeListener(token)

        assertEquals(1, maxConnectionCount)
        assertEquals(1, maxActiveCount)
        assertEquals(1, otherDB.count)

        stopListener()
        assertEquals(0, listener!!.status!!.connectionCount)
        assertEquals(0, listener!!.status!!.activeConnectionCount)
    }

    @Test
    fun testMultipleListenersOnSameDatabase() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(otherDB)
        val listener1 = URLEndpointListener(config)
        val listener2 = URLEndpointListener(config)

        listener1.start()
        listener2.start()

        createSingleDocInBaseTestDb("doc-1")
        run(
            listener1.localURLEndpoint,
            serverCert = listener1.tlsIdentity?.certs?.get(0)?.toByteArray()
        )

        // since listener1 and listener2 are using same certificates, one listener only needs stop.
        listener2.stop()
        stopListener(listener1)
        assertEquals(1, otherDB.count)
    }

    @Test
    fun testReplicatorAndListenerOnSameDatabase() = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)

        // listener
        val doc = MutableDocument()
        otherDB.save(doc)
        startListener()

        // Replicator#1 (otherDB -> DB#1)
        val doc1 = MutableDocument()
        baseTestDb.save(doc1)
        val target = DatabaseEndpoint(baseTestDb)
        val repl1 = createReplicator(otherDB, target)

        // Replicator#2 (DB#2 -> Listener(otherDB))
        Database.delete("db2")
        val db2 = Database("db2")
        val doc2 = MutableDocument()
        db2.save(doc2)
        val repl2 = createReplicator(
            db2,
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray()
        )

        val changeListener = { change: ReplicatorChange ->
            if (change.status.activityLevel == ReplicatorActivityLevel.IDLE &&
                change.status.progress.completed == change.status.progress.total) {
                if (otherDB.count == 3L && baseTestDb.count == 3L && db2.count == 3L) {
                    change.replicator.stop()
                }
            }

            if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                if (change.replicator.config.database.name == "db2") {
                    mutex2.unlock()
                } else {
                    mutex1.unlock()
                }
            }
        }
        val token1 = repl1.addChangeListener(changeListener)
        val token2 = repl2.addChangeListener(changeListener)

        repl1.start()
        repl2.start()
        withTimeout(10.seconds) { // TODO: FIXME
            mutex1.lock()
            mutex2.lock()
        }

        assertEquals(3, otherDB.count)
        assertEquals(3, baseTestDb.count)
        assertEquals(3, db2.count)

        repl1.removeChangeListener(token1)
        repl2.removeChangeListener(token2)

        db2.close()
        stopListener()
    }

    @Test
    fun testCloseWithActiveListener() {
        if (!keyChainAccessAllowed) return

        startListener()

        // Close database should also stop the listener:
        otherDB.close()

        assertNull(listener!!.port)
        assertTrue(listener!!.urls.isEmpty())

        stopListener()
    }

    @Test
    fun testEmptyNetworkInterface() {
        if (!keyChainAccessAllowed) return

        startListener()
        val urls = listener!!.urls

        /// Link local addresses cannot be assigned via network interface because they don't map to any given interface.
        val notLinkLocal = urls.filter { !it.contains("fe80::") && !it.contains(".local") }

        notLinkLocal.forEachIndexed { i, url ->
            // separate db instance!
            val db = Database("db-$i")
            val doc = MutableDocument()
            doc.setString("url", url)
            db.save(doc)

            // separate replicator instance
            val target = URLEndpoint(url)
            val rConfig = ReplicatorConfiguration(db, target)
            rConfig.pinnedServerCertificate = listener?.tlsIdentity?.certs?.get(0)?.toByteArray()
            run(rConfig)

            // remove the db
            db.delete()
        }

        assertEquals(otherDB.count, notLinkLocal.size.toLong())

        val q = QueryBuilder.select(SelectResult.all()).from(DataSource.database(otherDB))
        val rs = q.execute()
        val result = mutableListOf<String>()
        for (res in rs.allResults()) {
            val dict = res.getDictionary(0)!!
            result.add(dict.getString("url")!!)
        }

        assertEquals(notLinkLocal, result)
        stopListener()
    }

    @Test
    fun testMultipleReplicatorsToListener() {
        if (!keyChainAccessAllowed) return

        startListener()

        val doc = MutableDocument()
        doc.setString("species", "Tiger")
        otherDB.save(doc)

        // pushAndPull can cause race; so only push is validated
        validateMultipleReplicationsTo(listener!!, ReplicatorType.PUSH)

        stopListener()
    }

    @Test
    fun testMultipleReplicatorsToReadOnlyListener() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(otherDB)
        config.isReadOnly = true
        startListener(config)

        val doc = MutableDocument()
        doc.setString("species", "Tiger")
        otherDB.save(doc)

        validateMultipleReplicationsTo(listener!!, ReplicatorType.PULL)

        stopListener()
    }

    @Test
    fun testReadOnlyListener() {
        if (!keyChainAccessAllowed) return

        val doc1 = MutableDocument()
        baseTestDb.save(doc1)

        val config = URLEndpointListenerConfiguration(otherDB)
        config.isReadOnly = true
        startListener(config)

        run(
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray(),
            expectedError = CBLErrorHTTPForbidden.toInt()
        )
    }

    @Test
    fun testReplicatorServerCertificate() = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val x1 = Mutex(true)
        val x2 = Mutex(true)

        val listener = startListener()

        val serverCert = listener.tlsIdentity!!.certs[0]
        val serverCertData = serverCert.toByteArray()
        val repl = createReplicator(otherDB, listener.localURLEndpoint, serverCert = serverCertData)
        repl.addChangeListener { change ->
            val activity = change.status.activityLevel
            if (activity == ReplicatorActivityLevel.IDLE) {
                x1.unlock()
            } else if (activity == ReplicatorActivityLevel.STOPPED && change.status.error == null) {
                x2.unlock()
            }
        }
        assertNull(repl.serverCertificates)

        repl.start()

        withTimeout(5.seconds) {
            x1.lock()
        }
        var receivedServerCertData = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCertData)
        var receivedServerCert = receivedServerCertData.toSecCertificate()
        checkCertificateEqual(serverCert, receivedServerCert)

        repl.stop()

        withTimeout(5.seconds) {
            x2.lock()
        }
        receivedServerCertData = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCertData)
        receivedServerCert = receivedServerCertData.toSecCertificate()
        checkCertificateEqual(serverCert, receivedServerCert)

        stopListener()
    }

    @Test
    fun testReplicatorServerCertificateWithTLSError() = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        var x1 = Mutex(true)

        val listener = startListener()

        var serverCert = listener.tlsIdentity!!.certs[0]
        var repl = createReplicator(otherDB, listener.localURLEndpoint)
        repl.addChangeListener { change ->
            val activity = change.status.activityLevel
            if (activity == ReplicatorActivityLevel.STOPPED && change.status.error != null) {
                // TODO: https://issues.couchbase.com/browse/CBL-1471
                assertEquals(CBLErrorTLSCertUnknownRoot.toInt(), change.status.error!!.code)
                x1.unlock()
            }
        }
        assertNull(repl.serverCertificates)

        repl.start()

        withTimeout(5.seconds) {
            x1.lock()
        }
        var receivedServerCertData = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCertData)
        var receivedServerCert = receivedServerCertData.toSecCertificate()
        checkCertificateEqual(serverCert, receivedServerCert)

        // Use the receivedServerCert to pin:
        x1 = Mutex(true)
        val x2 = Mutex(true)
        serverCert = receivedServerCert
        repl = createReplicator(otherDB, listener.localURLEndpoint, serverCert = serverCert.toByteArray())
        repl.addChangeListener { change ->
            val activity = change.status.activityLevel
            if (activity == ReplicatorActivityLevel.IDLE) {
                x1.unlock()
            } else if (activity == ReplicatorActivityLevel.STOPPED && change.status.error == null) {
                x2.unlock()
            }
        }
        assertNull(repl.serverCertificates)

        repl.start()

        withTimeout(5.seconds) {
            x1.lock()
        }
        receivedServerCertData = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCertData)
        receivedServerCert = receivedServerCertData.toSecCertificate()
        checkCertificateEqual(serverCert, receivedServerCert)

        repl.stop()

        withTimeout(5.seconds) {
            x2.lock()
        }
        receivedServerCertData = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCertData)
        receivedServerCert = receivedServerCertData.toSecCertificate()
        checkCertificateEqual(serverCert, receivedServerCert)

        stopListener()
    }

    @Test
    fun testReplicatorServerCertificateWithTLSDisabled() = runBlocking {
        val x1 = Mutex(true)
        val x2 = Mutex(true)

        val listener = startListener(false)
        val repl = createReplicator(otherDB, listener.localURLEndpoint)
        repl.addChangeListener { change ->
            val activity = change.status.activityLevel
            if (activity == ReplicatorActivityLevel.IDLE) {
                x1.unlock()
            } else if (activity == ReplicatorActivityLevel.STOPPED && change.status.error == null) {
                x2.unlock()
            }
        }
        assertNull(repl.serverCertificates)

        repl.start()

        withTimeout(5.seconds) {
            x1.lock()
        }
        assertNull(repl.serverCertificates)

        repl.stop()

        withTimeout(5.seconds) {
            x2.lock()
        }
        assertNull(repl.serverCertificates)

        stopListener()
    }

    @Test
    fun testAcceptOnlySelfSignedServerCertificate() {
        if (!keyChainAccessAllowed) return

        // Listener:
        val listener = startListener()
        assertNotNull(listener.tlsIdentity)
        assertEquals(1, listener.tlsIdentity!!.certs.size)

        // Replicator - TLS Error:
        try {
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = false,
                expectedError = CBLErrorTLSCertUnknownRoot.toInt()
            )
        } catch (e: Exception) {
            // ignore
        }

        // Replicator - Success:
        try {
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = true
            )
        } catch (e: Exception) {
            // ignore
        }

        // Cleanup
        stopListener()
    }

    @Test
    fun testPinnedServerCertificate() {
        if (!keyChainAccessAllowed) return

        // Listener:
        val listener = startListener()
        assertNotNull(listener.tlsIdentity)
        assertEquals(1, listener.tlsIdentity!!.certs.size)

        // Replicator - TLS Error:
        try {
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = false,
                expectedError = CBLErrorTLSCertUnknownRoot.toInt()
            )
        } catch (e: Exception) {
            // ignore
        }

        // Replicator - Success:
        try {
            val serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = false,
                serverCert = serverCert
            )
        } catch (e: Exception) {
            // ignore
        }

        // Cleanup
        stopListener()
    }

    @Test
    fun testListenerWithImportIdentity() {
        if (!keyChainAccessAllowed) return

        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.buffer().readByteArray()
        }
        val identity = TLSIdentity.importIdentity(data, "123".toCharArray(), serverCertLabel)
        assertEquals(2, identity.certs.size)

        val config = URLEndpointListenerConfiguration(otherDB)
        config.tlsIdentity = identity

        try {
            startListener(config)
        } catch (e: Exception) {
            // ignore
        }

        assertNotNull(listener!!.tlsIdentity)
        assertTrue(identity == listener!!.tlsIdentity!!)

        createSingleDocInBaseTestDb("doc-1")
        assertEquals(0, otherDB.count)
        run(
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray()
        )
        assertEquals(1, otherDB.count)

        stopListener(listener!!)
        assertNull(listener!!.tlsIdentity)
    }

    @Test
    fun testStopListener() = runBlocking {
        val x1 = Mutex(true)
        val x2 = Mutex(true)

        // Listen:
        val listener = startListener(false)

        // Start replicator:
        val target = listener.localURLEndpoint
        val repl = createReplicator(otherDB, target)
        repl.addChangeListener { change ->
            val activity = change.status.activityLevel
            if (activity == ReplicatorActivityLevel.IDLE) {
                x1.unlock()
            } else if (activity == ReplicatorActivityLevel.STOPPED) {
                x2.unlock()
            }
        }
        repl.start()

        // Wait until idle then stop the listener:
        withTimeout(5.seconds) {
            x1.lock()
        }

        // Stop listen:
        stopListener()

        // Wait for the replicator to be stopped:
        withTimeout(5.seconds) {
            x2.lock()
        }

        // Check error:
        assertEquals(CBLErrorWebSocketGoingAway.toInt(), repl.status.error!!.code)

        // Check to ensure that the replicator is not accessible:
        run(target, maxAttempts = 2, expectedError = ECONNREFUSED)
    }

    @Test
    fun testTLSPasswordListenerAuthenticator() {
        if (!keyChainAccessAllowed) return

        val doc1 = MutableDocument()
        otherDB.save(doc1)

        // Listener:
        val auth = ListenerPasswordAuthenticator { username, password ->
            username == "daniel" && password.concatToString() == "123"
        }
        startListener(auth = auth)

        // Replicator - No Authenticator:
        run(
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray(),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Wrong Username:
        run(
            listener!!.localURLEndpoint,
            auth = BasicAuthenticator("daneil", "123".toCharArray()),
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray(),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Wrong Password:
        run(
            listener!!.localURLEndpoint,
            auth = BasicAuthenticator("daniel", "132".toCharArray()),
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray(),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Different ClientCertAuthenticator
        run(
            listener!!.localURLEndpoint,
            auth = ClientCertificateAuthenticator(createTLSIdentity(false)!!),
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray(),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // cleanup client cert authenticator identity
        TLSIdentity.deleteIdentity(clientCertLabel)

        // Replicator - Success:
        run(
            listener!!.localURLEndpoint,
            auth = BasicAuthenticator("daniel", "123".toCharArray()),
            serverCert = listener!!.tlsIdentity?.certs?.get(0)?.toByteArray()
        )
    }

    @Test
    fun testChainedCertServerAndCertPinning() {
        if (!keyChainAccessAllowed) return

        TLSIdentity.deleteIdentity(serverCertLabel)
        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.buffer().readByteArray()
        }
        val identity = TLSIdentity.importIdentity(data, "123".toCharArray(), serverCertLabel)
        assertEquals(2, identity.certs.size)

        val config = URLEndpointListenerConfiguration(otherDB)
        config.tlsIdentity = identity

        try {
            startListener(config)
        } catch (e: Exception) {
            // ignore
        }

        // pinning root cert should be successful
        run(
            listener!!.localURLEndpoint,
            serverCert = identity.certs[1].toByteArray()
        )

        // pinning leaf cert should be successful
        run(
            listener!!.localURLEndpoint,
            serverCert = identity.certs[0].toByteArray()
        )

        stopListener(listener!!)
        TLSIdentity.deleteIdentity(serverCertLabel)
    }

    // acceptSelfSignedOnly tests

    @Test
    fun testAcceptSelfSignedWithNonSelfSignedCert() {
        if (!keyChainAccessAllowed) return

        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.buffer().readByteArray()
        }
        val identity = TLSIdentity.importIdentity(data, "123".toCharArray(), serverCertLabel)
        assertEquals(2, identity.certs.size)

        val config = URLEndpointListenerConfiguration(otherDB)
        config.tlsIdentity = identity

        try {
            startListener(config)
        } catch (e: Exception) {
            // ignore
        }

        createSingleDocInBaseTestDb("doc-1")
        assertEquals(0, otherDB.count)

        // Reject the server with non-self-signed cert
        run(
            listener!!.localURLEndpoint,
            acceptSelfSignedOnly = true,
            expectedError = CBLErrorTLSCertUntrusted.toInt()
        )

        stopListener()
        TLSIdentity.deleteIdentity(serverCertLabel)
    }

    @Test
    fun testAcceptOnlySelfSignedCertificateWithPinnedCertificate() {
        if (!keyChainAccessAllowed) return

        // Listener:
        val listener = startListener()
        assertNotNull(listener.tlsIdentity)
        assertEquals(1, listener.tlsIdentity!!.certs.size)

        // listener = cert1; replicator.pin = cert2; acceptSelfSigned = true => fail
        TLSIdentity.deleteIdentity(serverCertLabel)
        val dummyTLSIdentity = createTLSIdentity()
        try {
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = true,
                serverCert = dummyTLSIdentity?.certs?.get(0)?.toByteArray(),
                expectedError = CBLErrorTLSCertUnknownRoot.toInt()
            )
        } catch (e: Exception) {
            // ignore
        }

        // listener = cert1; replicator.pin = cert1; acceptSelfSigned = false => pass
        try {
            run(
                listener.localURLEndpoint,
                serverCert = listener.tlsIdentity?.certs?.get(0)?.toByteArray()
            )
        } catch (e: Exception) {
            // ignore
        }

        // Cleanup
        stopListener()
        TLSIdentity.deleteIdentity(serverCertLabel)
    }

    // -- Close & Delete Replicators and Listeners

    @Test
    fun testCloseWithActiveReplicationsAndURLEndpointListener() {
        validateActiveReplicationsAndURLEndpointListener(false)
    }

    @Test
    fun testDeleteWithActiveReplicationsAndURLEndpointListener() {
        validateActiveReplicationsAndURLEndpointListener(true)
    }

    @Test
    fun testCloseWithActiveReplicatorAndURLEndpointListeners() {
        validateActiveReplicatorAndURLEndpointListeners(false)
    }

    @Test
    fun testDeleteWithActiveReplicatorAndURLEndpointListeners() {
        validateActiveReplicatorAndURLEndpointListeners(true)
    }

    // ListenerConfig

    @Test
    fun testSetListenerConfigurationProperties() {
        val config = URLEndpointListenerConfiguration(otherDB)
        val basic = ListenerPasswordAuthenticator { uname, pswd ->
            uname == "username" && pswd.concatToString() == "secret"
        }
        config.authenticator = basic
        config.isTlsDisabled = true
        config.isDeltaSyncEnabled = true
        config.networkInterface = "awesomeinterface.com"
        config.port = 3121
        config.isReadOnly = true
        if (keyChainAccessAllowed) {
            TLSIdentity.deleteIdentity(serverCertLabel)
            val tls = createTLSIdentity()
            config.tlsIdentity = tls
        }
        val listener = URLEndpointListener(config)

        // ----------
        // update config after passing to configuration’s constructor
        config.authenticator = null
        config.isTlsDisabled = false
        config.isDeltaSyncEnabled = false
        config.networkInterface = "0.0.0.0"
        config.port = 3123
        config.isReadOnly = false

        // update the returned config from listener
        val config2 = listener.config
        config2.authenticator = null
        config2.isTlsDisabled = false
        config2.isDeltaSyncEnabled = false
        config2.networkInterface = "0.0.0.0"
        config2.port = 3123
        config2.isReadOnly = false

        // validate no impact with above updates to configs
        assertNotNull(listener.config.authenticator)
        assertTrue(listener.config.isTlsDisabled)
        assertTrue(listener.config.isDeltaSyncEnabled)
        assertEquals("awesomeinterface.com", listener.config.networkInterface)
        assertEquals(3121, listener.config.port)
        assertTrue(listener.config.isReadOnly)

        if (keyChainAccessAllowed) {
            assertNotNull(listener.config.tlsIdentity)
            assertEquals(1, listener.config.tlsIdentity!!.certs.size)

            TLSIdentity.deleteIdentity(serverCertLabel)
        }
    }

    @Test
    fun testDefaultListenerConfiguration() {
        val config = URLEndpointListenerConfiguration(otherDB)

        assertFalse(config.isTlsDisabled)
        assertFalse(config.isDeltaSyncEnabled)
        assertFalse(config.isReadOnly)
        assertNull(config.authenticator)
        assertNull(config.networkInterface)
        assertNull(config.port)
        assertNull(config.tlsIdentity)
    }

    @Test
    fun testCopyingListenerConfiguration() {
        val config1 = URLEndpointListenerConfiguration(otherDB)

        val basic = ListenerPasswordAuthenticator { uname, pswd ->
            uname == "username" && pswd.concatToString() == "secret"
        }
        config1.authenticator = basic
        config1.isTlsDisabled = true
        config1.isDeltaSyncEnabled = true
        config1.networkInterface = "awesomeinterface.com"
        config1.port = 3121
        config1.isReadOnly = true

        if (keyChainAccessAllowed) {
            TLSIdentity.deleteIdentity(serverCertLabel)
            val tls = createTLSIdentity()
            config1.tlsIdentity = tls
        }
        val config = URLEndpointListenerConfiguration(config1)

        // ------
        // update config1 after passing to configuration’s constructor
        config1.authenticator = null
        config1.isTlsDisabled = false
        config1.isDeltaSyncEnabled = false
        config1.networkInterface = "0.0.0.0"
        config1.port = 3123
        config1.isReadOnly = false

        assertNotNull(config.authenticator)
        assertTrue(config.isTlsDisabled)
        assertTrue(config.isDeltaSyncEnabled)
        assertEquals("awesomeinterface.com", config.networkInterface)
        assertEquals(3121, config.port)
        assertTrue(config.isReadOnly)

        if (keyChainAccessAllowed) {
            assertNotNull(config.tlsIdentity)
            assertEquals(1, config.tlsIdentity!!.certs.size)

            TLSIdentity.deleteIdentity(serverCertLabel)
        }
    }
}

val URLEndpointListener.localURL: String
    get() {
        assertTrue(port != null && port!! > 0)
        val scheme = if (config.isTlsDisabled) "ws" else "wss"
        val host = "localhost"
        val path = config.database.name
        return "$scheme://$host:$port/$path"
    }

val URLEndpointListener.localURLEndpoint: URLEndpoint
    get() = URLEndpoint(localURL)
