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

import cocoapods.CouchbaseLite.CBLErrorHTTPNotFound
import kotbase.ext.toByteArray
import kotbase.ext.toNSData
import kotbase.ext.wrapError
import kotbase.test.lockWithTimeout
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import platform.Foundation.*
import platform.MultipeerConnectivity.*
import platform.darwin.NSObject
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

// TODO: Tests sometimes pass, but often fail:
@Ignore
class MessageEndpointAppleTest : BaseDbTest(), MultipeerConnectionDelegate {

    val nsObjectDelegate = NSObjectDelegate()

    lateinit var oDB: Database

    var clientPeer: MCPeerID? = null
    var serverPeer: MCPeerID? = null

    var clientSession: MCSession? = null
    var serverSession: MCSession? = null

    var browser: MCNearbyServiceBrowser? = null
    var advertiser: MCNearbyServiceAdvertiser? = null

    var clientConnection: MultipeerConnection? = null
    var serverConnection: MultipeerConnection? = null

    var listener: MessageEndpointListener? = null

    var clientConnected: Mutex? = null
    var serverConnected: Mutex? = null

    @BeforeTest
    fun setUp() {
        oDB = Database("otherdb")
    }

    @AfterTest
    fun tearDown() {
        listener?.closeAll()

        // Workaround to ensure that replicator's background cleaning task was done:
        // https://github.com/couchbase/couchbase-lite-core/issues/520
        runBlocking { delay(0.3.seconds) }

        browser?.stopBrowsingForPeers()
        advertiser?.stopAdvertisingPeer()

        clientSession?.disconnect()
        serverSession?.disconnect()

        oDB.close()
        deleteDb(oDB)
    }

    fun startDiscovery() = runBlocking {
        serverConnected = Mutex(true)
        serverPeer = MCPeerID("server")
        serverSession = MCSession(serverPeer!!, null, MCEncryptionNone)
        serverSession!!.delegate = nsObjectDelegate
        advertiser = MCNearbyServiceAdvertiser(serverPeer!!, null, "MyService")
        advertiser!!.delegate = nsObjectDelegate
        advertiser!!.startAdvertisingPeer()

        clientConnected = Mutex(true)
        clientPeer = MCPeerID("client")
        clientSession = MCSession(clientPeer!!, null, MCEncryptionNone)
        clientSession!!.delegate = nsObjectDelegate
        browser = MCNearbyServiceBrowser(clientPeer!!, "MyService")
        browser!!.delegate = nsObjectDelegate
        browser!!.startBrowsingForPeers()

        // cool down period(disconnected to next connected state), is taking around 4-10secs
        assertTrue(clientConnected!!.lockWithTimeout(30.seconds))
        assertTrue(serverConnected!!.lockWithTimeout(30.seconds))
    }

    fun run(config: ReplicatorConfiguration, expectedError: Int? = null) = runBlocking {
        // Start discovery:
        startDiscovery()

        // Start listener
        val x1 = Mutex(true)
        val x2 = Mutex(true)

        val listenerConfig = MessageEndpointListenerConfiguration(oDB, ProtocolType.MESSAGE_STREAM)

        listener = MessageEndpointListener(listenerConfig)
        val token1 = listener!!.addChangeListener { change ->
            val status = change.status
            if (status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
                x1.unlock()
            } else if (status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                x2.unlock()
            }
        }
        listener!!.accept(
            MultipeerConnection(serverSession!!, clientPeer!!, this@MessageEndpointAppleTest)
        )
        assertTrue(x1.lockWithTimeout(10.seconds))

        val x3 = Mutex(true)
        val repl = Replicator(config)
        val token2 = repl.addChangeListener { change ->
            val status = change.status
            if (config.isContinuous && status.activityLevel == ReplicatorActivityLevel.IDLE &&
                status.progress.completed == status.progress.total) {
                repl.stop()
            }

            if (status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                if (expectedError != null) {
                    val error = status.error
                    assertNotNull(error)
                    assertEquals(expectedError, error.code)
                }
                x3.unlock()
            }
        }

        repl.start()
        assertTrue(x3.lockWithTimeout(10.seconds))
        repl.stop()
        repl.removeChangeListener(token2)

        listener!!.closeAll()
        assertTrue(x2.lockWithTimeout(10.seconds))
        listener!!.removeChangeListener(token1)
    }

    fun config(
        target: Endpoint,
        type: ReplicatorType,
        continuous: Boolean
    ): ReplicatorConfiguration {
        val config = ReplicatorConfiguration(baseTestDb, target)
        config.type = type
        config.isContinuous = continuous
        return config
    }

    inner class NSObjectDelegate : NSObject(), MCNearbyServiceBrowserDelegateProtocol,
        MCNearbyServiceAdvertiserDelegateProtocol, MCSessionDelegateProtocol {

        // MCNearbyServiceBrowserDelegate

        override fun browser(
            browser: MCNearbyServiceBrowser,
            foundPeer: MCPeerID,
            withDiscoveryInfo: Map<Any?, *>?
        ) {
            browser.invitePeer(foundPeer, clientSession!!, null, 0.0)
        }

        override fun browser(browser: MCNearbyServiceBrowser, lostPeer: MCPeerID) {}

        // MCNearbyServiceAdvertiserDelegate

        override fun advertiser(
            advertiser: MCNearbyServiceAdvertiser,
            didReceiveInvitationFromPeer: MCPeerID,
            withContext: NSData?,
            invitationHandler: (Boolean, MCSession?) -> Unit
        ) {
            invitationHandler(true, serverSession)
        }

        // MCSessionDelegate

        override fun session(session: MCSession, peer: MCPeerID, didChangeState: MCSessionState) {
            when (didChangeState) {
                MCSessionState.MCSessionStateConnecting -> println("*** Connecting: ${peer.displayName}")
                MCSessionState.MCSessionStateConnected -> {
                    println("*** Connected: ${peer.displayName}")
                    if (session == serverSession) {
                        serverConnected!!.unlock()
                    } else {
                        clientConnected!!.unlock()
                    }
                }
                MCSessionState.MCSessionStateNotConnected -> println("*** Not Connected: ${peer.displayName}")
                else -> println("*** Unhandled State: $didChangeState")
            }
        }

        override fun session(session: MCSession, didReceiveData: NSData, fromPeer: MCPeerID) {
            println("*** Received ${didReceiveData.length} bytes from ${fromPeer.displayName} ")
            if (session == serverSession) {
                val serverConnection = serverConnection ?: run {
                    println("*** [ERR] server connection lost from ${fromPeer.displayName}")
                    return
                }

                serverConnection.receive(didReceiveData.toByteArray())

            } else {
                val clientConnection = clientConnection ?: run {
                    println("*** [ERR] client connection lost from ${fromPeer.displayName}")
                    return
                }

                clientConnection.receive(didReceiveData.toByteArray())
            }
        }

        override fun session(
            session: MCSession,
            didReceiveStream: NSInputStream,
            withName: String,
            fromPeer: MCPeerID
        ) { /* Not supported */ }

        override fun session(
            session: MCSession,
            didStartReceivingResourceWithName: String,
            fromPeer: MCPeerID,
            withProgress: NSProgress
        ) { /* Not supported */ }

        override fun session(
            session: MCSession,
            didFinishReceivingResourceWithName: String,
            fromPeer: MCPeerID,
            atURL: NSURL?,
            withError: NSError?
        ) { /* Not supported */ }
    }

    // MultipeerConnectionDelegate

    override fun connectionDidOpen(connection: MessageEndpointConnection) {
        connection as MultipeerConnection
        if (connection.session == serverSession) {
            serverConnection = connection
        } else {
            clientConnection = connection
        }
    }

    override fun connectionDidClose(connection: MessageEndpointConnection) {
        connection as MultipeerConnection
        if (connection.session == serverSession) {
            serverConnection = null
        } else {
            clientConnection = null
        }
    }

    // MessageEndpointDelegate

    val messageEndpointDelegate: MessageEndpointDelegate = {
        MultipeerConnection(clientSession!!, serverPeer!!, this)
    }

    // Tests

    @Test
    fun testPushDoc() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "Tiger")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "Cat")
        oDB.save(doc2)

        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
        val config = config(target, ReplicatorType.PUSH, false)
        run(config)

        assertEquals(2, oDB.count)
        val savedDoc = oDB.getDocument("doc1")!!
        assertEquals("Tiger", savedDoc.getString("name"))
    }

    @Test
    fun testPullDoc() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "Tiger")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "Cat")
        oDB.save(doc2)

        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
        val config = config(target, ReplicatorType.PULL, false)
        run(config)

        assertEquals(2, baseTestDb.count)
        val savedDoc = baseTestDb.getDocument("doc2")!!
        assertEquals("Cat", savedDoc.getString("name"))
    }

    @Test
    fun testPushPullDoc() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "Tiger")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "Cat")
        oDB.save(doc2)

        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
        val config = config(target, ReplicatorType.PUSH_AND_PULL, false)
        run(config)

        assertEquals(2, oDB.count)
        val savedDoc1 = oDB.getDocument("doc1")!!
        assertEquals("Tiger", savedDoc1.getString("name"))

        assertEquals(2, baseTestDb.count)
        val savedDoc2 = baseTestDb.getDocument("doc2")!!
        assertEquals("Cat", savedDoc2.getString("name"))
    }

    @Test
    fun testPushDocContinuous() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "Tiger")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "Cat")
        oDB.save(doc2)

        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
        val config = config(target, ReplicatorType.PUSH, true)
        run(config)

        assertEquals(2, oDB.count)
        val savedDoc = oDB.getDocument("doc1")!!
        assertEquals("Tiger", savedDoc.getString("name"))
    }

    @Test
    fun testPullDocContinuous() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "Tiger")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "Cat")
        oDB.save(doc2)

        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
        val config = config(target, ReplicatorType.PULL, true)
        run(config)

        assertEquals(2, baseTestDb.count)
        val savedDoc = baseTestDb.getDocument("doc2")!!
        assertEquals("Cat", savedDoc.getString("name"))
    }

    @Test
    fun testPushPullDocContinuous() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "Tiger")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "Cat")
        oDB.save(doc2)

        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
        val config = config(target, ReplicatorType.PUSH_AND_PULL, true)
        run(config)

        assertEquals(2, oDB.count)
        val savedDoc1 = oDB.getDocument("doc1")!!
        assertEquals("Tiger", savedDoc1.getString("name"))

        assertEquals(2, baseTestDb.count)
        val savedDoc2 = baseTestDb.getDocument("doc2")!!
        assertEquals("Cat", savedDoc2.getString("name"))
    }

    // TODO: 3.1 API
    // 8.16 MessageEndpointListener tests

//    @Test
//    fun testCollectionsSingleShotPushPullReplication() {
//        testCollectionsPushPullReplication(false)
//    }
//
//    @Test
//    fun testCollectionsContinuousPushPullReplication() {
//        testCollectionsPushPullReplication(true)
//    }
//
//    private fun testCollectionsPushPullReplication(continuous: Boolean) {
//        val col1a = baseTestDb.createCollection("colA", "scopeA")
//        val col1b = baseTestDb.createCollection("colB", "scopeA")
//
//        val col2a = oDB.createCollection("colA", "scopeA")
//        val col2b = oDB.createCollection("colB", "scopeA")
//
//        createDocNumbered(col1a, 0, 1)
//        createDocNumbered(col1b, 5, 2)
//        createDocNumbered(col2a, 10, 3)
//        createDocNumbered(col2b, 15, 5)
//        assertEquals(1, col1a.count)
//        assertEquals(2, col1b.count)
//        assertEquals(3, col2a.count)
//        assertEquals(5, col2b.count)
//
//        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
//        val config = ReplicatorConfiguration(target)
//        config.isContinuous = continuous
//        config.addCollections(listOf(col1a, col1b))
//
//        run(config, listOf(col2a, col2b))
//        assertEquals(4, col1a.count)
//        assertEquals(7, col1b.count)
//        assertEquals(4, col2a.count)
//        assertEquals(7, col2b.count)
//    }
//
//    @Test
//    fun testMismatchedCollectionReplication() {
//        val col1a = baseTestDb.createCollection("colA", "scopeA")
//        val col2b = oDB.createCollection("colB", "scopeA")
//
//        val target = MessageEndpoint("UID:123", null, ProtocolType.MESSAGE_STREAM, messageEndpointDelegate)
//        val config = ReplicatorConfiguration(target)
//        config.addCollections(listOf(col1a))
//
//        run(config, listOf(col2b), CBLErrorHTTPNotFound)
//    }
//
//    // Note: fatalError with this test
//    @Test
//    fun testCreateListenerConfigWithEmptyCollection() {
//        assertFailsWith<IllegalArgumentException> {
//            MessageEndpointListenerConfiguration(emptyList(), ProtocolType.MESSAGE_STREAM)
//        }
//    }
}

interface MultipeerConnectionDelegate {

    fun connectionDidOpen(connection: MessageEndpointConnection)

    fun connectionDidClose(connection: MessageEndpointConnection)
}

class MultipeerConnection(
    val session: MCSession,
    val peerID: MCPeerID,
    val delegate: MultipeerConnectionDelegate
) : MessageEndpointConnection {

    lateinit var replConnection: ReplicatorConnection

    fun receive(data: ByteArray) {
        replConnection.receive(Message.fromData(data))
    }

    override fun open(
        connection: ReplicatorConnection,
        completion: (Boolean, MessagingError?) -> Unit
    ) {
        replConnection = connection
        delegate.connectionDidOpen(this)
        completion(true, null)
    }

    override fun close(error: Exception?, completion: MessagingCloseCompletion) {
        session.disconnect()
        delegate.connectionDidClose(this)
        completion()
    }

    override fun send(message: Message, completion: (Boolean, MessagingError?) -> Unit) {
        try {
            wrapError { error ->
                session.sendData(
                    message.toData().toNSData(),
                    listOf(peerID),
                    MCSessionSendDataMode.MCSessionSendDataReliable,
                    error
                )
            }
            completion(true, null)
        } catch (e: Exception) {
            completion(false, MessagingError(e, false))
        }
    }
}
