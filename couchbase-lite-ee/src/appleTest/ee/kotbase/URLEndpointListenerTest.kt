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
@file:Suppress("DEPRECATION")

package kotbase

import cocoapods.CouchbaseLite.*
import kotbase.ext.toSecCertificate
import kotbase.internal.utils.PlatformUtils
import kotbase.test.lockWithTimeout
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.readByteArray
import platform.CoreFoundation.CFStringRefVar
import platform.Foundation.CFBridgingRelease
import platform.Security.SecCertificateCopyCommonName
import platform.Security.errSecSuccess
import platform.posix.EADDRINUSE
import platform.posix.ECONNREFUSED
import kotlin.math.max
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

// TODO: many tests fail with
//  CouchbaseLiteException{CouchbaseLite,22,'Couldn't add a certificate to the Keychain (SecItemAdd returned -25299)'}
//  or
//  kotlin.NullPointerException
//  https://youtrack.jetbrains.com/issue/KT-61470#focus=Comments-27-8789456.0-0 may be helpful
// TODO: identities are not cleaned up in keychain in URLEndpointListenerBaseTest (should be in finally {} block)
@Ignore
@OptIn(ExperimentalStdlibApi::class)
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
            input.readByteArray()
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
            listener.tlsIdentity!!.certs[0]
        )
        val repl2 = createReplicator(
            db2,
            listener.localURLEndpoint,
            false,
            type,
            listener.tlsIdentity?.certs?.get(0)
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
        assertTrue(mutex1.lockWithTimeout(5.seconds))
        assertTrue(mutex2.lockWithTimeout(5.seconds))

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
        testDatabase.save(doc1)
        val doc2 = MutableDocument("other-db-doc")
        targetDatabase.save(doc2)

        // start listener
        startListener()

        // replicator#1
        val repl1 = createReplicator(targetDatabase, DatabaseEndpoint(testDatabase))

        // replicator#2
        Database.delete("db2")
        val db2 = Database("db2")
        val repl2 = createReplicator(
            db2,
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)
        )

        val changeListener = { change: ReplicatorChange ->
            if (change.status.activityLevel == ReplicatorActivityLevel.IDLE
                && change.status.progress.completed == change.status.progress.total
            ) {
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
        // TODO: FIXME
        assertTrue(idleMutex1.lockWithTimeout(10.seconds))
        assertTrue(idleMutex2.lockWithTimeout(10.seconds))

        if (isDeleteDBs) {
            db2.delete()
            targetDatabase.delete()
        } else {
            db2.close()
            targetDatabase.close()
        }

        // TODO: FIXME
        assertTrue(stopMutex1.lockWithTimeout(10.seconds))
        assertTrue(stopMutex2.lockWithTimeout(10.seconds))
        repl1.removeChangeListener(token1)
        repl2.removeChangeListener(token2)
        stopListener()
    }

    private fun validateActiveReplicatorAndURLEndpointListeners(isDeleteDB: Boolean) = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val idleMutex = Mutex(true)
        val stopMutex = Mutex(true)

        val config = URLEndpointListenerConfiguration(targetDatabase)
        val listener1 = URLEndpointListener(config)
        val listener2 = URLEndpointListener(config)

        // listener
        listener1.start()
        listener2.start()

        val doc1 = MutableDocument("db-doc")
        testDatabase.save(doc1)
        val doc2 = MutableDocument("other-db-doc")
        targetDatabase.save(doc2)

        // replicator
        val repl1 = createReplicator(
            targetDatabase,
            listener1.localURLEndpoint,
            serverCert = listener1.tlsIdentity?.certs?.get(0)
        )
        val token1 = repl1.addChangeListener { change ->
            if (change.status.activityLevel == ReplicatorActivityLevel.IDLE
                && change.status.progress.completed == change.status.progress.total
            ) {
                idleMutex.unlock()

            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                stopMutex.unlock()
            }
        }
        repl1.start()
        assertTrue(idleMutex.lockWithTimeout(5.seconds))

        if (isDeleteDB) {
            targetDatabase.delete()
        } else {
            targetDatabase.close()
        }

        assertTrue(stopMutex.lockWithTimeout(5.seconds))

        // cleanup
        repl1.removeChangeListener(token1)
        stopListener(listener1)
        stopListener(listener2)
    }

    // -- Tests

    @Test
    fun testPort() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(targetDatabase)
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

        val config = URLEndpointListenerConfiguration(targetDatabase)
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

        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.port = listener!!.port
        val listener2 = URLEndpointListener(config)

        assertThrowsCBLException(CBLError.Domain.POSIX, EADDRINUSE) {
            listener2.start()
        }
    }

    @Test
    fun testURLs() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(targetDatabase)
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
        targetDatabase.save(doc)

        val config = URLEndpointListenerConfiguration(targetDatabase)
        val listener = URLEndpointListener(config)
        assertNull(listener.tlsIdentity)
        listener.start()
        assertNotNull(listener.tlsIdentity)

        // anonymous identity
        run(
            listener.localURLEndpoint,
            serverCert = listener.tlsIdentity?.certs?.get(0)
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
            serverCert = tlsID.certs[0],
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
        targetDatabase.save(doc)

        val tls = createTLSIdentity()
        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.tlsIdentity = tls
        val listener = URLEndpointListener(config)
        assertNull(listener.tlsIdentity)
        listener.start()
        assertNotNull(listener.tlsIdentity)

        run(
            listener.localURLEndpoint,
            serverCert = listener.tlsIdentity?.certs?.get(0)
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
            serverCert = tlsID.certs[0],
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
        val serverCert = listener.tlsIdentity?.certs?.get(0)
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
        val serverCert = listener.tlsIdentity?.certs?.get(0)
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
            input.readByteArray()
        }

        // Listener:
        val listenerAuth = ListenerCertificateAuthenticator(listOf(rootCert))
        val listener = startListener(auth = listenerAuth)

        // Cleanup:
        TLSIdentity.deleteIdentity(clientCertLabel)

        // Create client identity:
        val clientCertData = PlatformUtils.getAsset("identity/client.p12")!!.use { input ->
            input.readByteArray()
        }
        val identity = TLSIdentity.importIdentity(
            clientCertData,
            "123".toCharArray(),
            clientCertLabel
        )

        // Replicator:
        val auth = ClientCertificateAuthenticator(identity)
        val serverCert = listener.tlsIdentity?.certs?.get(0)

        try {
            run(listener.localURLEndpoint, auth = auth, serverCert = serverCert)
        } catch (_: Exception) {
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
            input.readByteArray()
        }

        // Listener:
        val listenerAuth = ListenerCertificateAuthenticator(listOf(rootCert))
        val listener = startListener(auth = listenerAuth)

        // Replicator:
        val auth = ClientCertificateAuthenticator(createTLSIdentity(false)!!)
        val serverCert = listener.tlsIdentity?.certs?.get(0)

        try {
            run(
                listener.localURLEndpoint,
                auth = auth,
                serverCert = serverCert,
                expectedError = CBLErrorTLSClientCertRejected.toInt()
            )
        } catch (_: Exception) {
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
        val config = URLEndpointListenerConfiguration(targetDatabase)
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
        targetDatabase.save(doc1)

        var maxConnectionCount = 0
        var maxActiveCount = 0
        val rConfig = ReplicatorConfiguration(testDatabase, listener!!.localURLEndpoint)
        rConfig.type = ReplicatorType.PULL
        rConfig.isContinuous = false
        rConfig.pullFilter = { _, _ ->
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
        assertTrue(pullFilterBusy.lockWithTimeout(5.seconds))
        assertTrue(replicatorStop.lockWithTimeout(5.seconds))
        repl.removeChangeListener(token)

        assertEquals(1, maxConnectionCount)
        assertEquals(1, maxActiveCount)
        assertEquals(1, targetDatabase.count)

        stopListener()
        assertEquals(0, listener!!.status!!.connectionCount)
        assertEquals(0, listener!!.status!!.activeConnectionCount)
    }

    @Test
    fun testMultipleListenersOnSameDatabase() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(targetDatabase)
        val listener1 = URLEndpointListener(config)
        val listener2 = URLEndpointListener(config)

        listener1.start()
        listener2.start()

        testDatabase.save(MutableDocument("doc-1"))
        run(
            listener1.localURLEndpoint,
            serverCert = listener1.tlsIdentity?.certs?.get(0)
        )

        // since listener1 and listener2 are using same certificates, one listener only needs stop.
        listener2.stop()
        stopListener(listener1)
        assertEquals(1, targetDatabase.count)
    }

    @Test
    fun testReplicatorAndListenerOnSameDatabase() = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)

        // listener
        val doc = MutableDocument()
        targetDatabase.save(doc)
        startListener()

        // Replicator#1 (targetDatabase -> DB#1)
        val doc1 = MutableDocument()
        testDatabase.save(doc1)
        val target = DatabaseEndpoint(testDatabase)
        val repl1 = createReplicator(targetDatabase, target)

        // Replicator#2 (DB#2 -> Listener(targetDatabase))
        Database.delete("db2")
        val db2 = Database("db2")
        val doc2 = MutableDocument()
        db2.save(doc2)
        val repl2 = createReplicator(
            db2,
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)
        )

        val changeListener = { change: ReplicatorChange ->
            if (change.status.activityLevel == ReplicatorActivityLevel.IDLE &&
                change.status.progress.completed == change.status.progress.total
            ) {
                if (targetDatabase.count == 3L && testDatabase.count == 3L && db2.count == 3L) {
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
        // TODO: FIXME
        assertTrue(mutex1.lockWithTimeout(10.seconds))
        assertTrue(mutex2.lockWithTimeout(10.seconds))

        assertEquals(3, targetDatabase.count)
        assertEquals(3, testDatabase.count)
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
        targetDatabase.close()

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
            rConfig.pinnedServerCertificate = listener?.tlsIdentity?.certs?.get(0)
            rConfig.run()

            // remove the db
            db.delete()
        }

        assertEquals(targetDatabase.count, notLinkLocal.size.toLong())

        val q = QueryBuilder.select(SelectResult.all()).from(DataSource.database(targetDatabase))
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
        targetDatabase.save(doc)

        // pushAndPull can cause race; so only push is validated
        validateMultipleReplicationsTo(listener!!, ReplicatorType.PUSH)

        stopListener()
    }

    @Test
    fun testMultipleReplicatorsToReadOnlyListener() {
        if (!keyChainAccessAllowed) return

        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.isReadOnly = true
        startListener(config)

        val doc = MutableDocument()
        doc.setString("species", "Tiger")
        targetDatabase.save(doc)

        validateMultipleReplicationsTo(listener!!, ReplicatorType.PULL)

        stopListener()
    }

    @Test
    fun testReadOnlyListener() {
        if (!keyChainAccessAllowed) return

        val doc1 = MutableDocument()
        testDatabase.save(doc1)

        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.isReadOnly = true
        startListener(config)

        run(
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0),
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
        val repl = createReplicator(targetDatabase, listener.localURLEndpoint, serverCert = serverCert)
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

        assertTrue(x1.lockWithTimeout(5.seconds))
        var receivedServerCert = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCert)
        checkCertificateEqual(serverCert, receivedServerCert)

        repl.stop()

        assertTrue(x2.lockWithTimeout(5.seconds))
        receivedServerCert = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCert)
        checkCertificateEqual(serverCert, receivedServerCert)

        stopListener()
    }

    @Test
    fun testReplicatorServerCertificateWithTLSError() = runBlocking {
        if (!keyChainAccessAllowed) return@runBlocking

        var x1 = Mutex(true)

        val listener = startListener()

        var serverCert = listener.tlsIdentity!!.certs[0]
        var repl = createReplicator(targetDatabase, listener.localURLEndpoint)
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

        assertTrue(x1.lockWithTimeout(5.seconds))
        var receivedServerCert = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCert)
        checkCertificateEqual(serverCert, receivedServerCert)

        // Use the receivedServerCert to pin:
        x1 = Mutex(true)
        val x2 = Mutex(true)
        serverCert = receivedServerCert
        repl = createReplicator(
            targetDatabase,
            listener.localURLEndpoint,
            serverCert = serverCert
        )
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

        assertTrue(x1.lockWithTimeout(5.seconds))
        receivedServerCert = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCert)
        checkCertificateEqual(serverCert, receivedServerCert)

        repl.stop()

        assertTrue(x2.lockWithTimeout(5.seconds))
        receivedServerCert = repl.serverCertificates?.get(0)
        assertNotNull(receivedServerCert)
        checkCertificateEqual(serverCert, receivedServerCert)

        stopListener()
    }

    @Test
    fun testReplicatorServerCertificateWithTLSDisabled() = runBlocking {
        val x1 = Mutex(true)
        val x2 = Mutex(true)

        val listener = startListener(false)
        val repl = createReplicator(targetDatabase, listener.localURLEndpoint)
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

        assertTrue(x1.lockWithTimeout(5.seconds))
        assertNull(repl.serverCertificates)

        repl.stop()

        assertTrue(x2.lockWithTimeout(5.seconds))
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
        } catch (_: Exception) {
            // ignore
        }

        // Replicator - Success:
        try {
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = true
            )
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            // ignore
        }

        // Replicator - Success:
        try {
            val serverCert = listener.tlsIdentity?.certs?.get(0)
            run(
                listener.localURLEndpoint,
                acceptSelfSignedOnly = false,
                serverCert = serverCert
            )
        } catch (_: Exception) {
            // ignore
        }

        // Cleanup
        stopListener()
    }

    @Test
    fun testListenerWithImportIdentity() {
        if (!keyChainAccessAllowed) return

        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.readByteArray()
        }
        val identity = TLSIdentity.importIdentity(data, "123".toCharArray(), serverCertLabel)
        assertEquals(2, identity.certs.size)

        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.tlsIdentity = identity

        try {
            startListener(config)
        } catch (_: Exception) {
            // ignore
        }

        assertNotNull(listener!!.tlsIdentity)
        assertEquals(identity, listener!!.tlsIdentity!!)

        testDatabase.save(MutableDocument("doc-1"))
        assertEquals(0, targetDatabase.count)
        run(
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0)
        )
        assertEquals(1, targetDatabase.count)

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
        val repl = createReplicator(targetDatabase, target)
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
        assertTrue(x1.lockWithTimeout(5.seconds))

        // Stop listen:
        stopListener()

        // Wait for the replicator to be stopped:
        assertTrue(x2.lockWithTimeout(5.seconds))

        // Check error:
        assertEquals(CBLErrorWebSocketGoingAway.toInt(), repl.status.error!!.code)

        // Check to ensure that the replicator is not accessible:
        run(target, maxAttempts = 2, expectedError = ECONNREFUSED)
    }

    @Test
    fun testTLSPasswordListenerAuthenticator() {
        if (!keyChainAccessAllowed) return

        val doc1 = MutableDocument()
        targetDatabase.save(doc1)

        // Listener:
        val auth = ListenerPasswordAuthenticator { username, password ->
            username == "daniel" && password.concatToString() == "123"
        }
        startListener(auth = auth)

        // Replicator - No Authenticator:
        run(
            listener!!.localURLEndpoint,
            serverCert = listener!!.tlsIdentity?.certs?.get(0),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Wrong Username:
        run(
            listener!!.localURLEndpoint,
            auth = BasicAuthenticator("daneil", "123".toCharArray()),
            serverCert = listener!!.tlsIdentity?.certs?.get(0),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Wrong Password:
        run(
            listener!!.localURLEndpoint,
            auth = BasicAuthenticator("daniel", "132".toCharArray()),
            serverCert = listener!!.tlsIdentity?.certs?.get(0),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // Replicator - Different ClientCertAuthenticator
        run(
            listener!!.localURLEndpoint,
            auth = ClientCertificateAuthenticator(createTLSIdentity(false)!!),
            serverCert = listener!!.tlsIdentity?.certs?.get(0),
            expectedError = CBLErrorHTTPAuthRequired.toInt()
        )

        // cleanup client cert authenticator identity
        TLSIdentity.deleteIdentity(clientCertLabel)

        // Replicator - Success:
        run(
            listener!!.localURLEndpoint,
            auth = BasicAuthenticator("daniel", "123".toCharArray()),
            serverCert = listener!!.tlsIdentity?.certs?.get(0)
        )
    }

    @Test
    fun testChainedCertServerAndCertPinning() {
        if (!keyChainAccessAllowed) return

        TLSIdentity.deleteIdentity(serverCertLabel)
        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.readByteArray()
        }
        val identity = TLSIdentity.importIdentity(data, "123".toCharArray(), serverCertLabel)
        assertEquals(2, identity.certs.size)

        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.tlsIdentity = identity

        try {
            startListener(config)
        } catch (_: Exception) {
            // ignore
        }

        // pinning root cert should be successful
        run(
            listener!!.localURLEndpoint,
            serverCert = identity.certs[1]
        )

        // pinning leaf cert should be successful
        run(
            listener!!.localURLEndpoint,
            serverCert = identity.certs[0]
        )

        stopListener(listener!!)
        TLSIdentity.deleteIdentity(serverCertLabel)
    }

    // acceptSelfSignedOnly tests

    @Test
    fun testAcceptSelfSignedWithNonSelfSignedCert() {
        if (!keyChainAccessAllowed) return

        val data = PlatformUtils.getAsset("identity/certs.p12")!!.use { input ->
            input.readByteArray()
        }
        val identity = TLSIdentity.importIdentity(data, "123".toCharArray(), serverCertLabel)
        assertEquals(2, identity.certs.size)

        val config = URLEndpointListenerConfiguration(targetDatabase)
        config.tlsIdentity = identity

        try {
            startListener(config)
        } catch (_: Exception) {
            // ignore
        }

        testDatabase.save(MutableDocument("doc-1"))
        assertEquals(0, targetDatabase.count)

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
                serverCert = dummyTLSIdentity?.certs?.get(0),
                expectedError = CBLErrorTLSCertUnknownRoot.toInt()
            )
        } catch (_: Exception) {
            // ignore
        }

        // listener = cert1; replicator.pin = cert1; acceptSelfSigned = false => pass
        try {
            run(
                listener.localURLEndpoint,
                serverCert = listener.tlsIdentity?.certs?.get(0)
            )
        } catch (_: Exception) {
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
        val config = URLEndpointListenerConfiguration(targetDatabase)
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
        val config = URLEndpointListenerConfiguration(targetDatabase)

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
        val config1 = URLEndpointListenerConfiguration(targetDatabase)

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
