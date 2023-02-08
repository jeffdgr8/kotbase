package com.couchbase.lite.kmp

import com.udobny.kmp.test.IgnoreApple
import com.udobny.kmp.test.IgnoreNative
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class ReplicatorEETest : BaseReplicatorTest() {

    // ReplicatorTest.swift

    @Test
    fun testEmptyPush() {
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, false)
        run(config)
    }

    @Test
    fun testStartWithCheckpoint() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "striped")
        baseTestDb.save(doc2)

        // Push:
        val target = DatabaseEndpoint(otherDB)
        var config = makeConfig(target, ReplicatorType.PUSH, false)
        run(config)

        // Pull:
        config = makeConfig(target, ReplicatorType.PULL, false)
        run(config)

        assertEquals(2, baseTestDb.count)

        var doc = baseTestDb.getDocument("doc1")!!
        baseTestDb.purge(doc)

        doc = baseTestDb.getDocument("doc2")!!
        baseTestDb.purge(doc)

        assertEquals(0, baseTestDb.count)

        // Pull again, shouldn't have any new changes:
        run(config)
        assertEquals(0, baseTestDb.count)

        // Reset and pull:
        run(config, reset = true)
        assertEquals(2, baseTestDb.count)
    }

    @Test
    fun testStartWithResetCheckpointContinuous() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "striped")
        baseTestDb.save(doc2)

        // Push:
        val target = DatabaseEndpoint(otherDB)
        var config = makeConfig(target, ReplicatorType.PUSH, true)
        run(config)

        // Pull:
        config = makeConfig(target, ReplicatorType.PULL, true)
        run(config)

        assertEquals(2, baseTestDb.count)

        var doc = baseTestDb.getDocument("doc1")!!
        baseTestDb.purge(doc)

        doc = baseTestDb.getDocument("doc2")!!
        baseTestDb.purge(doc)

        assertEquals(0, baseTestDb.count)

        // Pull again, shouldn't have any new changes:
        run(config)
        assertEquals(0, baseTestDb.count)

        // Reset and pull:
        run(config, reset = true)
        assertEquals(2, baseTestDb.count)
    }

    @Test
    fun testDocumentReplicationEvent() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "Striped")
        baseTestDb.save(doc2)

        // Push:
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, false)

        lateinit var replicator: Replicator
        lateinit var token: ListenerToken
        val docs = mutableListOf<ReplicatedDocument>()
        run(config) { r ->
            replicator = r
            token = r.addDocumentReplicationListener { replication ->
                assertTrue(replication.isPush)
                docs.addAll(replication.documents)
            }
        }

        // Check if getting two document replication events:
        assertEquals(2, docs.size)
        assertEquals("doc1", docs[0].id)
        assertNull(docs[0].error)
        assertFalse(docs[0].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[0].flags.contains(DocumentFlag.ACCESS_REMOVED))

        assertEquals("doc2", docs[1].id)
        assertNull(docs[1].error)
        assertFalse(docs[1].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[1].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Add another doc:
        val doc3 = MutableDocument("doc3")
        doc3.setString("species", "Tiger")
        doc3.setString("pattern", "Star")
        baseTestDb.save(doc3)

        // Run the replicator again:
        run(replicator)

        // Check if getting a new document replication event:
        assertEquals(3, docs.size)
        assertEquals("doc3", docs[2].id)
        assertNull(docs[2].error)
        assertFalse(docs[2].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[2].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Add another doc:
        val doc4 = MutableDocument("doc4")
        doc4.setString("species", "Tiger")
        doc4.setString("pattern", "WhiteStriped")
        baseTestDb.save(doc4)

        // Remove document replication listener:
        replicator.removeChangeListener(token)

        // Run the replicator again:
        run(replicator)

        // Should not getting a new document replication event:
        assertEquals(3, docs.size)
    }

    @Test
    fun testDocumentReplicationEventWithPushConflict() {
        val doc1a = MutableDocument("doc1")
        doc1a.setString("species", "Tiger")
        doc1a.setString("pattern", "Star")
        baseTestDb.save(doc1a)

        val doc1b = MutableDocument("doc1")
        doc1b.setString("species", "Tiger")
        doc1b.setString("pattern", "Striped")
        otherDB.save(doc1b)

        // Push:
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, false)

        lateinit var replicator: Replicator
        lateinit var token: ListenerToken
        val docs = mutableListOf<ReplicatedDocument>()
        run(config) { r ->
            replicator = r
            token = r.addDocumentReplicationListener { replication ->
                assertTrue(replication.isPush)
                docs.addAll(replication.documents)
            }
        }

        // Check:
        assertEquals(1, docs.size)
        assertEquals("doc1", docs[0].id)
        val err = docs[0].error
        assertNotNull(err)
        assertEquals(CBLError.Domain.CBLITE, err.domain)
        assertEquals(CBLError.Code.HTTP_CONFLICT, err.code)
        assertFalse(docs[0].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[0].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Remove document replication listener:
        replicator.removeChangeListener(token)
    }

    @Test
    fun testDocumentReplicationEventWithPullConflict() {
        val doc1a = MutableDocument("doc1")
        doc1a.setString("species", "Tiger")
        doc1a.setString("pattern", "Star")
        baseTestDb.save(doc1a)

        val doc1b = MutableDocument("doc1")
        doc1b.setString("species", "Tiger")
        doc1b.setString("pattern", "Striped")
        otherDB.save(doc1b)

        // Pull:
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PULL, false)

        lateinit var replicator: Replicator
        lateinit var token: ListenerToken
        val docs = mutableListOf<ReplicatedDocument>()
        run(config) { r ->
            replicator = r
            token = r.addDocumentReplicationListener { replication ->
                assertFalse(replication.isPush)
                docs.addAll(replication.documents)
            }
        }

        // Check:
        assertEquals(1, docs.size)
        assertEquals("doc1", docs[0].id)
        assertNull(docs[0].error)
        assertFalse(docs[0].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[0].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Remove document replication listener:
        replicator.removeChangeListener(token)
    }

    @Test
    fun testDocumentReplicationEventWithDeletion() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Star")
        baseTestDb.save(doc1)

        // Delete:
        baseTestDb.delete(doc1)

        // Push:
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, false)

        lateinit var replicator: Replicator
        lateinit var token: ListenerToken
        val docs = mutableListOf<ReplicatedDocument>()
        run(config) { r ->
            replicator = r
            token = r.addDocumentReplicationListener { replication ->
                assertTrue(replication.isPush)
                docs.addAll(replication.documents)
            }
        }

        // Check:
        assertEquals(1, docs.size)
        assertEquals("doc1", docs[0].id)
        assertNull(docs[0].error)
        assertTrue(docs[0].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[0].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Remove document replication listener:
        replicator.removeChangeListener(token)
    }

    // Failing on Apple and jvm
    @Test
    fun testSingleShotPushFilter() {
        testPushFilter(false)
    }

    // Failing on Apple and jvm
    @Test
    fun testContinuousPushFilter() {
        testPushFilter(true)
    }

    private fun testPushFilter(isContinuous: Boolean) {
        // Create documents:
        val content = "I'm a tiger.".encodeToByteArray()
        val blob = Blob("text/plain", content)

        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        doc1.setBlob("photo", blob)
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "Striped")
        doc2.setBlob("photo", blob)
        baseTestDb.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("species", "Tiger")
        doc3.setString("pattern", "Star")
        doc3.setBlob("photo", blob)
        baseTestDb.save(doc3)
        baseTestDb.delete(doc3)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, isContinuous)
        config.pushFilter = { doc, flags ->
            assertNotNull(doc.id)
            val isDeleted = flags.contains(DocumentFlag.DELETED)
            assertTrue(if (doc.id == "doc3") isDeleted else !isDeleted)
            if (!isDeleted) {
                // Check content:
                assertNotNull(doc.getValue("pattern"))
                assertEquals("Tiger", doc.getString("species"))

                // Check blob:
                val photo = doc.getBlob("photo")
                assertNotNull(photo)
                assertEquals(blob, photo)
            } else {
                assertEquals(emptyMap(), doc.toMap())
            }

            // Gather document ID:
            docIds.add(doc.id)

            // Reject doc2:
            doc.id != "doc2"
        }

        // Fails on Apple
        // Run the replicator:
        run(config)

        // Check documents passed to the filter:
        assertEquals(3, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("doc2"))
        assertTrue(docIds.contains("doc3"))

        // Check replicated documents:
        assertNotNull(otherDB.getDocument("doc1"))
        assertNull(otherDB.getDocument("doc2"))
        assertNull(otherDB.getDocument("doc3"))
    }

    // native C fails sometimes
    // kotlin.AssertionError: Expected <3>, actual <2>.
    @Test
    fun testPullFilter() {
        // Add a document to db database so that it can pull the deleted docs from:
        val doc0 = MutableDocument("doc0")
        doc0.setString("species", "Cat")
        baseTestDb.save(doc0)

        // Create documents:
        val content = "I'm a tiger.".encodeToByteArray()
        val blob = Blob("text/plain", content)

        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        doc1.setBlob("photo", blob)
        otherDB.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "Striped")
        doc2.setBlob("photo", blob)
        otherDB.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("species", "Tiger")
        doc3.setString("pattern", "Star")
        doc3.setBlob("photo", blob)
        otherDB.save(doc3)
        otherDB.delete(doc3)

        // Create replicator with pull filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PULL, false)
        config.pullFilter = { doc, flags ->
            assertNotNull(doc.id)
            val isDeleted = flags.contains(DocumentFlag.DELETED)
            assertTrue(if (doc.id == "doc3") isDeleted else !isDeleted)
            if (!isDeleted) {
                // Check content:
                assertNotNull(doc.getValue("pattern"))
                assertEquals("Tiger", doc.getString("species"))

                // Check blob:
                val photo = doc.getBlob("photo")
                assertNotNull(photo)

                // Note: Cannot access content because there is no actual blob file saved on disk.
                // assertEquals(blob.content, photo.content)
            } else {
                assertEquals(emptyMap(), doc.toMap())
            }

            // Gather document ID:
            docIds.add(doc.id)

            // Reject doc2:
            doc.id != "doc2"
        }

        // Run the replicator:
        run(config)

        // Check documents passed to the filter:
        assertEquals(3, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("doc2"))
        assertTrue(docIds.contains("doc3"))

        // Check replicated documents:
        assertNotNull(baseTestDb.getDocument("doc1"))
        assertNull(baseTestDb.getDocument("doc2"))
        assertNull(baseTestDb.getDocument("doc3"))
    }

    @Test
    fun testPushAndForget() = runBlocking {
        val doc = MutableDocument("doc1")
        doc.setString("species", "Tiger")
        doc.setString("pattern", "Hobbes")
        baseTestDb.save(doc)

        val mutex = Mutex(true)
        val docChangeToken = baseTestDb.addDocumentChangeListener(doc.id) { change ->
            assertEquals(change.documentID, doc.id)
            if (baseTestDb.getDocument(doc.id) == null) {
                mutex.unlock()
            }
        }
        assertEquals(1, baseTestDb.count)
        assertEquals(0, otherDB.count)

        // Push:
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, false)
        lateinit var replicator: Replicator
        lateinit var docReplicationToken: ListenerToken
        run(config) { r ->
            replicator = r
            docReplicationToken = r.addDocumentReplicationListener {
                baseTestDb.setDocumentExpiration(doc.id, Clock.System.now())
            }
        }

        withTimeout(5.seconds) {
            mutex.lock()
        }

        replicator.removeChangeListener(docReplicationToken)
        baseTestDb.removeChangeListener(docChangeToken)

        assertEquals(0, baseTestDb.count)
        assertEquals(1, otherDB.count)
    }

    // Removed Doc with Filter

    // native C fails sometimes
    // kotlin.AssertionError: Expected <2>, actual <1>.
    @Test
    fun testPullRemovedDocWithFilterSingleShot() {
        testPullRemovedDocWithFilter(false)
    }

    @Test
    fun testPullRemovedDocWithFilterContinuous() {
        testPullRemovedDocWithFilter(true)
    }

    private fun testPullRemovedDocWithFilter(isContinuous: Boolean) {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        otherDB.save(doc1)

        val doc2 = MutableDocument("pass")
        doc2.setString("name", "pass")
        otherDB.save(doc2)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PULL, isContinuous)
        config.pullFilter = { doc, flags ->
            assertNotNull(doc.id)

            val isAccessRemoved = flags.contains(DocumentFlag.ACCESS_REMOVED)
            if (isAccessRemoved) {
                docIds.add(doc.id)
                doc.id == "pass"
            } else {
                doc.getString("name") == "pass"
            }
        }

        // Run the replicator:
        run(config)
        assertEquals(0, docIds.size)

        assertNotNull(baseTestDb.getDocument("doc1"))
        assertNotNull(baseTestDb.getDocument("pass"))

        val doc1Mutable = otherDB.getDocument("doc1")?.toMutable() ?: error("Docs must exists")
        doc1Mutable.setData(mapOf("_removed" to true))
        otherDB.save(doc1Mutable)

        val doc2Mutable = otherDB.getDocument("pass")?.toMutable() ?: error("Docs must exists")
        doc2Mutable.setData(mapOf("_removed" to true))
        otherDB.save(doc2Mutable)

        run(config)

        // Check documents passed to the filter:
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("pass"))

        assertNotNull(baseTestDb.getDocument("doc1"))
        assertNull(baseTestDb.getDocument("pass"))
    }

    // Deleted Doc with Filter

    // TODO: https://issues.couchbase.com/browse/CBL-2771
    @Test
    fun testPushDeletedDocWithFilterSingleShot() {
        testPushDeletedDocWithFilter(false)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2771
    @Test
    fun testPushDeletedDocWithFilterContinuous() {
        testPushDeletedDocWithFilter(true)
    }

    @Test
    fun testPullDeletedDocWithFilterSingleShot() {
        testPullDeletedDocWithFilter(false)
    }

    // native C fails sometimes
    // kotlin.AssertionError: Expected <2>, actual <1>.
    // or kotlin.AssertionError: Expected value to be true.
    @Test
    fun testPullDeletedDocWithFilterContinuous() {
        testPullDeletedDocWithFilter(true)
    }

    private fun testPushDeletedDocWithFilter(isContinuous: Boolean) {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        baseTestDb.save(doc1)

        val doc2 = MutableDocument("pass")
        doc2.setString("name", "pass")
        baseTestDb.save(doc2)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, isContinuous)
        config.pushFilter = { doc, flags ->
            assertNotNull(doc.id)

            val isDeleted = flags.contains(DocumentFlag.DELETED)
            if (isDeleted) {
                docIds.add(doc.id)
                doc.id == "pass"
            } else {
                doc.getString("name") == "pass"
            }
        }

        // Run the replicator:
        run(config)
        assertEquals(0, docIds.size)

        assertNotNull(otherDB.getDocument("doc1"))
        assertNotNull(otherDB.getDocument("pass"))

        baseTestDb.delete(doc1)
        baseTestDb.delete(doc2)

        run(config)

        // Check documents passed to the filter:
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("pass"))

        assertNotNull(otherDB.getDocument("doc1"))
        assertNull(otherDB.getDocument("pass"))
    }

    private fun testPullDeletedDocWithFilter(isContinuous: Boolean) {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        otherDB.save(doc1)

        val doc2 = MutableDocument("pass")
        doc2.setString("name", "pass")
        otherDB.save(doc2)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PULL, isContinuous)
        config.pullFilter = { doc, flags ->
            assertNotNull(doc.id)

            val isDeleted = flags.contains(DocumentFlag.DELETED)
            if (isDeleted) {
                docIds.add(doc.id)
                doc.id == "pass"
            } else {
                doc.getString("name") == "pass"
            }
        }

        // Run the replicator:
        run(config)
        assertEquals(0, docIds.size)

        assertNotNull(baseTestDb.getDocument("doc1"))
        assertNotNull(baseTestDb.getDocument("pass"))

        otherDB.delete(doc1)
        otherDB.delete(doc2)

        run(config)

        // Check documents passed to the filter:
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("pass"))

        assertNotNull(baseTestDb.getDocument("doc1"))
        assertNull(baseTestDb.getDocument("pass"))
    }

    // stop and restart replication with filter

    // https://issues.couchbase.com/browse/CBL-1061
    @Test
    fun testStopAndRestartPushReplicationWithFilter() {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        baseTestDb.save(doc1)

        // Create replicator with pull filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PUSH, true)
        config.pushFilter = { doc, _ ->
            assertNotNull(doc.id)
            docIds.add(doc.id)
            doc.getString("name") == "pass"
        }

        // create a replicator
        baseTestReplicator = Replicator(config)
        run(baseTestReplicator!!)

        assertEquals(1, docIds.size)
        assertEquals(1, otherDB.count)
        assertEquals(1, baseTestDb.count)

        // make some more changes
        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "pass")
        baseTestDb.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("name", "donotpass")
        baseTestDb.save(doc3)

        // restart the same replicator
        docIds.clear()
        run(baseTestReplicator!!)

        // should use the same replicator filter.
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc3"))
        assertTrue(docIds.contains("doc2"))

        assertNotNull(otherDB.getDocument("doc1"))
        // Fails on jvm
        assertNotNull(otherDB.getDocument("doc2"))
        assertNull(otherDB.getDocument("doc3"))
        assertEquals(3, baseTestDb.count)
        assertEquals(2, otherDB.count)
    }

    // native C fails sometimes
    // kotlin.AssertionError: Expected <2>, actual <1>.
    @Test
    fun testStopAndRestartPullReplicationWithFilter() {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        otherDB.save(doc1)

        // Create replicator with pull filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(otherDB)
        val config = makeConfig(target, ReplicatorType.PULL, true)
        config.pullFilter = { doc, _ ->
            assertNotNull(doc.id)
            docIds.add(doc.id)
            doc.getString("name") == "pass"
        }

        // create a replicator
        baseTestReplicator = Replicator(config)
        run(baseTestReplicator!!)

        assertEquals(1, docIds.size)
        assertEquals(1, otherDB.count)
        assertEquals(1, baseTestDb.count)

        // make some more changes
        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "pass")
        otherDB.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("name", "donotpass")
        otherDB.save(doc3)

        // restart the same replicator
        docIds.clear()
        // fails on jvm
        run(baseTestReplicator!!)

        // should use the same replicator filter.
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc3"))
        assertTrue(docIds.contains("doc2"))

        assertNotNull(baseTestDb.getDocument("doc1"))
        assertNotNull(baseTestDb.getDocument("doc2"))
        assertNull(baseTestDb.getDocument("doc3"))
        assertEquals(3, otherDB.count)
        assertEquals(2, baseTestDb.count)
    }

    // ReplicatorTest+PendingDocIds.swift

    private val kActionKey = "action-key"
    private var noOfDocument = 5
    private val kCreateActionValue = "doc-create"
    private val kUpdateActionValue = "doc-update"

    // Helper methods

    /**
     * create docs : [doc-1, doc-2, ...] up to `noOfDocument` docs.
     */
    private fun createDocs(): MutableSet<String> {
        val docIds = mutableSetOf<String>()
        for (i in 0 until noOfDocument) {
            val doc = MutableDocument("doc-$i")
            doc.setValue(kActionKey, kCreateActionValue)
            saveDocInBaseTestDb(doc)
            docIds.add("doc-$i")
        }
        return docIds
    }

    private fun validatePendingDocumentIDs(
        docIds: Set<String>,
        config: ReplicatorConfiguration? = null
    ) = runBlocking {
        val mutex = Mutex(true)
        val replConfig = config ?: makeConfig(DatabaseEndpoint(otherDB), ReplicatorType.PUSH, false)
        val replicator = Replicator(replConfig)

        // verify before starting the replicator
        assertEquals(docIds.size, replicator.getPendingDocumentIds().size)
        assertEquals(docIds, replicator.getPendingDocumentIds())

        var first = true
        val token = replicator.addChangeListener { change ->
            val pDocIds = change.replicator.getPendingDocumentIds()

            // TODO: native C does not receive ReplicatorActivityLevel.CONNECTING
            //if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
            if (first) {
                first = false
                assertEquals(docIds, pDocIds)
                assertEquals(docIds.size, pDocIds.size)
            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                assertEquals(0, pDocIds.size)
                mutex.unlock()
            }
        }

        replicator.start()
        withTimeout(5.seconds) {
            mutex.lock()
        }
        replicator.removeChangeListener(token)
    }

    /**
     * expected: [docId: isPresent] e.g., @{"doc-1": true, "doc-2": false, "doc-3": false}
     */
    private fun validateIsDocumentPending(
        expected: Map<String, Boolean>,
        config: ReplicatorConfiguration? = null
    ) = runBlocking {
        val mutex = Mutex(true)
        val replConfig = config ?: makeConfig(DatabaseEndpoint(otherDB), ReplicatorType.PUSH, false)
        val replicator = Replicator(replConfig)

        // verify before starting the replicator
        for ((docId, present) in expected) {
            assertEquals(present, replicator.isDocumentPending(docId))
        }

        var first = true
        val token = replicator.addChangeListener { change ->
            // TODO: native C does not receive ReplicatorActivityLevel.CONNECTING
            //if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
            if (first) {
                first = false
                for ((docId, present) in expected) {
                    assertEquals(present, replicator.isDocumentPending(docId))
                }
            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                for ((docId, _) in expected) {
                    assertFalse(replicator.isDocumentPending(docId))
                }
                mutex.unlock()
            }
        }

        replicator.start()
        withTimeout(5.seconds) {
            mutex.lock()
        }
        replicator.removeChangeListener(token)
    }

    // Unit Tests

    @Test
    fun testPendingDocIDsPullOnlyException() = runBlocking {
        val mutex = Mutex(true)
        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PULL, false)
        val replicator = Replicator(replConfig)

        var first = true
        var pullOnlyError: CouchbaseLiteException? = null
        val token = replicator.addChangeListener { change ->
            // TODO: native C does not receive ReplicatorActivityLevel.CONNECTING
            //if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
            if (first) {
                first = false
                try {
                    replicator.getPendingDocumentIds()
                } catch (e: CouchbaseLiteException) {
                    pullOnlyError = e
                }
            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                mutex.unlock()
            }
        }
        replicator.start()
        withTimeout(5.seconds) {
            mutex.lock()
        }

        assertEquals(CBLError.Code.UNSUPPORTED, pullOnlyError?.code)
        replicator.removeChangeListener(token)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2448
    @Test
    fun testPendingDocIDsWithCreate() {
        val docIds = createDocs()
        validatePendingDocumentIDs(docIds)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2448
    @Test
    fun testPendingDocIDsWithUpdate() {
        createDocs()

        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PUSH, false)
        run(replConfig)

        val updatedIds = setOf("doc-2", "doc-4")
        for (docId in updatedIds) {
            val doc = baseTestDb.getDocument(docId)!!.toMutable()
            doc.setString(kActionKey, kUpdateActionValue)
            saveDocInBaseTestDb(doc)
        }

        validatePendingDocumentIDs(updatedIds)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2448
    @Test
    fun testPendingDocIdsWithDelete() {
        createDocs()

        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PUSH, false)
        run(replConfig)

        val deletedIds = setOf("doc-2", "doc-4")
        for (docId in deletedIds) {
            val doc = baseTestDb.getDocument(docId)!!
            baseTestDb.delete(doc)
        }

        validatePendingDocumentIDs(deletedIds)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2448
    @Test
    fun testPendingDocIdsWithPurge() {
        val docs = createDocs()

        baseTestDb.purge("doc-3")
        docs.remove("doc-3")

        validatePendingDocumentIDs(docs)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2448
    @Test
    fun testPendingDocIdsWithFilter() {
        createDocs()

        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PUSH, false)
        replConfig.pushFilter = { doc, _ ->
            doc.id == "doc-3"
        }

        validatePendingDocumentIDs(setOf("doc-3"), replConfig)
    }

    // isDocumentPending

    @Test
    fun testIsDocumentPendingPullOnlyException() = runBlocking {
        val mutex = Mutex(true)
        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PULL, false)
        val replicator = Replicator(replConfig)

        var first = true
        var pullOnlyError: CouchbaseLiteException? = null
        val token = replicator.addChangeListener { change ->
            // TODO: native C does not receive ReplicatorActivityLevel.CONNECTING
            //if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
            if (first) {
                first = false
                try {
                    replicator.isDocumentPending("doc-1")
                } catch (e: CouchbaseLiteException) {
                    pullOnlyError = e
                }
            } else if (change.status.activityLevel == ReplicatorActivityLevel.STOPPED) {
                mutex.unlock()
            }
        }

        replicator.start()
        withTimeout(5.seconds) {
            mutex.lock()
        }

        assertEquals(CBLError.Code.UNSUPPORTED, pullOnlyError?.code)
        replicator.removeChangeListener(token)
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2575
    @Test
    fun testIsDocumentPendingWithCreate() {
        noOfDocument = 2
        createDocs()

        validateIsDocumentPending(mapOf("doc-0" to true, "doc-1" to true, "doc-3" to false))
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2575
    @Test
    fun testIsDocumentPendingWithUpdate() {
        createDocs()

        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PUSH, false)
        run(replConfig)

        val updatedIds = setOf("doc-2", "doc-4")
        for (docId in updatedIds) {
            val doc = baseTestDb.getDocument(docId)!!.toMutable()
            doc.setString(kActionKey, kUpdateActionValue)
            saveDocInBaseTestDb(doc)
        }

        validateIsDocumentPending(mapOf("doc-2" to true, "doc-4" to true, "doc-1" to false))
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2575
    @Test
    fun testIsDocumentPendingWithDelete() {
        createDocs()

        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PUSH, false)
        run(replConfig)

        val deletedIds = setOf("doc-2", "doc-4")
        for (docId in deletedIds) {
            val doc = baseTestDb.getDocument(docId)!!
            baseTestDb.delete(doc)
        }

        validateIsDocumentPending(mapOf("doc-2" to true, "doc-4" to true, "doc-1" to false))
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2575
    @Test
    fun testIsDocumentPendingWithPurge() {
        noOfDocument = 3
        createDocs()

        baseTestDb.purge("doc-1")

        validateIsDocumentPending(mapOf("doc-0" to true, "doc-1" to false, "doc-2" to true))
    }

    // TODO: https://issues.couchbase.com/browse/CBL-2575
    @Test
    fun testIsDocumentPendingWithPushFilter() {
        createDocs()

        val target = DatabaseEndpoint(otherDB)
        val replConfig = makeConfig(target, ReplicatorType.PUSH, false)
        replConfig.pushFilter = { doc, _ ->
            doc.id == "doc-3"
        }

        validateIsDocumentPending(mapOf("doc-3" to true, "doc-1" to false), replConfig)
    }

    // ReplicatorTest+CustomConflict.swift

    @Test
    fun testConflictResolverConfigProperty() {
        val target = URLEndpoint("wss://foo")
        val pullConfig = makeConfig(target, ReplicatorType.PULL, false)

        val conflictResolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        pullConfig.conflictResolver = conflictResolver
        baseTestReplicator = Replicator(pullConfig)

        assertNotNull(pullConfig.conflictResolver)
        assertNotNull(baseTestReplicator!!.config.conflictResolver)
    }

    private fun getConfig(type: ReplicatorType): ReplicatorConfiguration {
        val target = DatabaseEndpoint(otherDB)
        return makeConfig(target, type, false)
    }

    private fun makeConflict(
        docID: String,
        localData: Map<String, Any?>?,
        remoteData: Map<String, Any?>?
    ) {
        // create doc
        val doc = MutableDocument(docID)
        saveDocInBaseTestDb(doc)

        // sync the doc in both DBs.
        val config = getConfig(ReplicatorType.PUSH)
        run(config)

        // Now make different changes in db and oDBs
        if (localData != null) {
            val doc1a = baseTestDb.getDocument(docID)!!.toMutable()
            doc1a.setData(localData)
            saveDocInBaseTestDb(doc1a)
        } else {
            baseTestDb.delete(baseTestDb.getDocument(docID)!!)
        }

        if (remoteData != null) {
            val doc1b = otherDB.getDocument(docID)!!.toMutable()
            doc1b.setData(remoteData)
            otherDB.save(doc1b)
        } else {
            otherDB.delete(otherDB.getDocument(docID)!!)
        }
    }

    @Test
    fun testConflictResolverRemoteWins() {
        val localData = mapOf("name" to "Hobbes")
        val remoteData = mapOf("pattern" to "striped")
        makeConflict("doc", localData, remoteData)

        val config = getConfig(ReplicatorType.PULL)
        val resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(1, baseTestDb.count)
        assertEquals(baseTestDb.getDocument("doc")!!, resolver.winner!!)
        assertEquals(remoteData, baseTestDb.getDocument("doc")!!.toMap())
    }

    @Test
    fun testConflictResolverLocalWins() {
        val localData = mapOf("name" to "Hobbes")
        val remoteData = mapOf("pattern" to "striped")
        makeConflict("doc", localData, remoteData)

        val config = getConfig(ReplicatorType.PULL)
        val resolver = TestConflictResolver { conflict ->
            conflict.localDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(1, baseTestDb.count)
        assertEquals(baseTestDb.getDocument("doc")!!, resolver.winner!!)
        assertEquals(localData, baseTestDb.getDocument("doc")!!.toMap())
    }

    @Test
    fun testConflictResolverNullDoc() {
        val localData = mapOf("name" to "Hobbes")
        val remoteData = mapOf("pattern" to "striped")
        makeConflict("doc", localData, remoteData)

        val config = getConfig(ReplicatorType.PULL)
        val resolver = TestConflictResolver {
            null
        }
        config.conflictResolver = resolver
        run(config)

        assertNull(resolver.winner)
        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument("doc"))
    }

    @Test
    fun testConflictResolverDeletedLocalWins() {
        val remoteData = mapOf("key2" to "value2")
        makeConflict("doc", null, remoteData)

        val config = getConfig(ReplicatorType.PULL)
        val resolver = TestConflictResolver { conflict ->
            assertNull(conflict.localDocument)
            assertNotNull(conflict.remoteDocument)
            null
        }
        config.conflictResolver = resolver
        run(config)

        assertNull(resolver.winner)
        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument("doc"))
    }

    @Test
    fun testConflictResolverDeletedRemoteWins() {
        val localData = mapOf("key1" to "value1")
        makeConflict("doc", localData, null)

        val config = getConfig(ReplicatorType.PULL)
        val resolver = TestConflictResolver { conflict ->
            assertNotNull(conflict.localDocument)
            assertNull(conflict.remoteDocument)
            null
        }
        config.conflictResolver = resolver
        run(config)

        assertNull(resolver.winner)
        assertEquals(0, baseTestDb.count)
        assertNull(baseTestDb.getDocument("doc"))
    }

    @Test
    fun testConflictResolverCalledTwice() {
        val docID = "doc"
        val localData = mapOf<String, Any?>("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        makeConflict(docID, localData, remoteData)
        var count = 0
        val resolver = TestConflictResolver { conflict ->
            count += 1

            // update the doc will cause a second conflict
            val savedDoc = baseTestDb.getDocument(docID)!!.toMutable()
            if (!savedDoc["secondUpdate"].exists) {
                savedDoc.setBoolean("secondUpdate", true)
                baseTestDb.save(savedDoc)
            }

            val mDoc = conflict.localDocument!!.toMutable()
            mDoc.setString("edit", "local")
            mDoc
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(2, count)
        assertEquals(1, baseTestDb.count)
        val expectedDocDict = localData.toMutableMap()
        expectedDocDict["edit"] = "local"
        expectedDocDict["secondUpdate"] = true
        assertEquals(expectedDocDict, baseTestDb.getDocument(docID)!!.toMap())
    }

    @Test
    fun testConflictResolverMergeDoc() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        // EDIT LOCAL DOCUMENT
        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver { conflict ->
            val doc = conflict.localDocument?.toMutable()
            doc?.setString("edit", "local")
            doc
        }
        config.conflictResolver = resolver
        run(config)

        var expectedDocDict = localData.toMutableMap()
        expectedDocDict["edit"] = "local"
        assertEquals(expectedDocDict, baseTestDb.getDocument(docID)!!.toMap())

        // EDIT REMOTE DOCUMENT
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            val doc = conflict.remoteDocument?.toMutable()
            doc?.setString("edit", "remote")
            doc
        }
        config.conflictResolver = resolver
        run(config)

        expectedDocDict = remoteData.toMutableMap()
        expectedDocDict["edit"] = "remote"
        assertEquals(expectedDocDict, baseTestDb.getDocument(docID)!!.toMap())

        // CREATE NEW DOCUMENT
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            val doc = MutableDocument(conflict.localDocument!!.id)
            doc.setString("docType", "new-with-same-ID")
            doc
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(
            mapOf("docType" to "new-with-same-ID"),
            baseTestDb.getDocument(docID)!!.toMap()
        )
    }

    @Test
    fun testDocumentReplicationEventForConflictedDocs() {
        // when resolution is skipped: here doc from oDB throws an exception & skips it
        var resolver = TestConflictResolver {
            otherDB.getDocument("doc")
        }
        validateDocumentReplicationEventForConflictedDocs(resolver)

        // when resolution is successful but wrong docID
        resolver = TestConflictResolver {
            MutableDocument()
        }
        validateDocumentReplicationEventForConflictedDocs(resolver)

        // when resolution is successful.
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        validateDocumentReplicationEventForConflictedDocs(resolver)
    }

    private fun validateDocumentReplicationEventForConflictedDocs(resolver: TestConflictResolver) {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        config.conflictResolver = resolver

        makeConflict(docID, localData, remoteData)

        lateinit var token: ListenerToken
        lateinit var replicator: Replicator
        val docIds = mutableListOf<String>()
        run(config) { r ->
            replicator = r
            token = r.addDocumentReplicationListener { docRepl ->
                for (doc in docRepl.documents) {
                    docIds.add(doc.id)
                }
            }
        }

        // make sure only single listener event is fired when conflict occured.
        assertEquals(1, docIds.size)
        assertEquals(docID, docIds.first())
        replicator.removeChangeListener(token)

        // resolve any un-resolved conflict through pull replication.
        run(getConfig(ReplicatorType.PULL))
    }

    @Test
    fun testConflictResolverWrongDocID() {
        // use this to verify the logs generated during the conflict resolution.
        val customLogger = CustomLogger()
        customLogger.level = LogLevel.WARNING
        Database.log.custom = customLogger

        val docID = "doc"
        val wrongDocID = "wrong-doc-id"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        makeConflict(docID, localData, remoteData)
        val resolver = TestConflictResolver {
            val mDoc = MutableDocument(wrongDocID)
            mDoc.setString("edit", "update")
            mDoc
        }
        config.conflictResolver = resolver
        lateinit var token: ListenerToken
        lateinit var replicator: Replicator
        val docIds = mutableSetOf<String>()
        run(config) { repl ->
            replicator = repl
            token = repl.addDocumentReplicationListener { docRepl ->
                if (docRepl.documents.isNotEmpty()) {
                    assertEquals(1, docRepl.documents.size)
                    docIds.add(docRepl.documents.first().id)
                }

                // shouldn't report an error from replicator
                assertNull(docRepl.documents.firstOrNull()?.error)
            }
        }
        replicator.removeChangeListener(token)

        // validate wrong doc-id is resolved successfully
        assertEquals(1, baseTestDb.count)
        assertTrue(docIds.contains(docID))
        assertEquals(mapOf("edit" to "update"), baseTestDb.getDocument(docID)!!.toMap())

        // validate the warning log
        assertTrue(
            customLogger.lines.contains( // iOS log
                "The document ID of the resolved document '$wrongDocID' " +
                        "is not matching with the document ID of the conflicting " +
                        "document '$docID'."
            ) || customLogger.lines.contains( // Java log
                "[JAVA] The ID of the document produced by conflict resolution " +
                        "for document ($wrongDocID) does not match the IDs of " +
                        "the conflicting documents ($docID)"
            ) || customLogger.lines.contains( // Native C log
                "The document ID '$wrongDocID' of the resolved document is not " +
                        "matching with the document ID '$docID' of the conflicting document."
            )
        )

        Database.log.custom = null
    }

    @Test
    fun testConflictResolverDifferentDBDoc() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver {
            otherDB.getDocument(docID) // doc from different DB!!
        }
        config.conflictResolver = resolver
        lateinit var token: ListenerToken
        lateinit var replicator: Replicator
        var error: CouchbaseLiteException? = null

        run(config) { repl ->
            replicator = repl
            token = repl.addDocumentReplicationListener { docRepl ->
                val err = docRepl.documents.firstOrNull()?.error
                if (err != null) {
                    error = err
                }
            }
        }
        assertNotNull(error)
        assertTrue(
            error!!.code == CBLError.Code.UNEXPECTED_ERROR || // Java uses this code
                    error!!.code == CBLError.Code.CONFLICT || // iOS uses this code
                    error!!.code == CBLError.Code.INVALID_PARAMETER // Native C uses this code
        )
        assertEquals(CBLError.Domain.CBLITE, error!!.domain)

        replicator.removeChangeListener(token)
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)
        assertEquals(remoteData, baseTestDb.getDocument(docID)!!.toMap())
    }

    /// disabling since, exceptions inside conflict handler will leak, since objc doesn't perform release
    /// when exception happens
    // TODO: Kotlin Exception without @Throws(), which resolve() interface lacks,
    //  and NSException both unable to be forwarded to Objective-C or Native C caller
    @IgnoreApple
    @IgnoreNative
    @Test
    fun testConflictResolverThrowingException() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver {
            throw IllegalStateException("some exception happened inside custom conflict resolution")
        }
        config.conflictResolver = resolver
        lateinit var token: ListenerToken
        lateinit var replicator: Replicator
        var error: CouchbaseLiteException? = null

        run(config) { repl ->
            replicator = repl
            token = repl.addDocumentReplicationListener { docRepl ->
                val err = docRepl.documents.firstOrNull()?.error
                if (err != null) {
                    error = err
                    assertEquals(CBLError.Code.CONFLICT, err.code)
                    assertEquals(CBLError.Domain.CBLITE, err.domain)
                }
            }
        }

        assertNotNull(error)
        replicator.removeChangeListener(token)
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)
        assertEquals(remoteData, baseTestDb.getDocument(docID)!!.toMap())
    }

    @Test
    fun testConflictResolutionDefault() {
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        // higher generation-id
        var docID = "doc1"
        makeConflict(docID, localData, remoteData)
        var doc = baseTestDb.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        saveDocInBaseTestDb(doc)

        // delete local
        docID = "doc2"
        makeConflict(docID, localData, remoteData)
        baseTestDb.delete(baseTestDb.getDocument(docID)!!)
        doc = otherDB.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        otherDB.save(doc)

        // delete remote
        docID = "doc3"
        makeConflict(docID, localData, remoteData)
        doc = baseTestDb.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        baseTestDb.save(doc)
        otherDB.delete(otherDB.getDocument(docID)!!)

        // delete local but higher remote generation
        docID = "doc4"
        makeConflict(docID, localData, remoteData)
        baseTestDb.delete(baseTestDb.getDocument(docID)!!)
        doc = otherDB.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        otherDB.save(doc)
        doc = otherDB.getDocument(docID)!!.toMutable()
        doc.setString("key4", "value4")
        otherDB.save(doc)

        val config = getConfig(ReplicatorType.PULL)
        config.conflictResolver = ReplicatorConfiguration.DEFAULT_CONFLICT_RESOLVER
        run(config)

        // validate saved doc includes the key3, which is the highest generation.
        assertEquals("value3", baseTestDb.getDocument("doc1")?.getString("key3"))

        // validates the deleted doc is chosen for its counterpart doc which saved
        assertNull(baseTestDb.getDocument("doc2"))
        assertNull(baseTestDb.getDocument("doc3"))

        // validates the deleted doc is chosen without considering the generation.
        assertNull(baseTestDb.getDocument("doc4"))
    }

    @Test
    fun testConflictResolverReturningBlob() {
        val docID = "doc"
        val content = "I am a blob".encodeToByteArray()
        var blob = Blob("text/plain", content)

        val config = getConfig(ReplicatorType.PULL)

        // RESOLVE WITH REMOTE and BLOB data in LOCAL
        var localData = mapOf("key1" to "value1", "blob" to blob)
        var remoteData = mapOf<String, Any>("key2" to "value2")
        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertNull(baseTestDb.getDocument(docID)?.getBlob("blob"))
        assertEquals(remoteData, baseTestDb.getDocument(docID)!!.toMap())

        // RESOLVE WITH LOCAL with BLOB data
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            conflict.localDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(blob, baseTestDb.getDocument(docID)?.getBlob("blob"))
        assertEquals("value1", baseTestDb.getDocument(docID)?.getString("key1"))

        // RESOLVE WITH LOCAL and BLOB data in REMOTE
        blob = Blob("text/plain", content)
        localData = mapOf("key1" to "value1")
        remoteData = mapOf("key2" to "value2", "blob" to blob)
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            conflict.localDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertNull(baseTestDb.getDocument(docID)?.getBlob("blob"))
        assertEquals(localData, baseTestDb.getDocument(docID)!!.toMap())

        // RESOLVE WITH REMOTE with BLOB data
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(blob, baseTestDb.getDocument(docID)?.getBlob("blob"))
        assertEquals("value2", baseTestDb.getDocument(docID)?.getString("key2"))
    }

    @Test
    fun testConflictResolverReturningBlobFromDifferentDB() {
        val docID = "doc"
        val content = "I am a blob".encodeToByteArray()
        val blob = Blob("text/plain", content)
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2", "blob" to blob)
        val config = getConfig(ReplicatorType.PULL)

        // using remote document blob is okay to use!
        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver { conflict ->
            val mDoc = conflict.localDocument?.toMutable()
            mDoc?.setBlob("blob", conflict.remoteDocument?.getBlob("blob"))
            mDoc
        }
        config.conflictResolver = resolver
        lateinit var token: ListenerToken
        lateinit var replicator: Replicator
        run(config) { repl ->
            replicator = repl
            token = repl.addDocumentReplicationListener { docRepl ->
                assertNull(docRepl.documents.firstOrNull()?.error)
            }
        }
        replicator.removeChangeListener(token)

        // using blob from remote document of user's- which is a different database
        val oDBDoc = otherDB.getDocument(docID)!!
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            val mDoc = conflict.localDocument?.toMutable()
            mDoc?.setBlob("blob", oDBDoc.getBlob("blob"))
            mDoc
        }
        config.conflictResolver = resolver
        var error: CouchbaseLiteException? = null
        run(config) { repl ->
            replicator = repl
            token = repl.addDocumentReplicationListener { docRepl ->
                val err = docRepl.documents.firstOrNull()?.error
                if (err != null) {
                    error = err
                }
            }
        }
        assertNotNull(error)
        assertEquals(CBLError.Code.UNEXPECTED_ERROR, error?.code)
        assertTrue(
            error!!.message!!.contains(
                "A document contains a blob that was saved to a different " +
                        "database. The save operation cannot complete."
            )
        )
        replicator.removeChangeListener(token)
    }

    @Test
    fun testNonBlockingDatabaseOperationConflictResolver() {
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)
        makeConflict("doc1", localData, remoteData)

        var count = 0
        val resolver = TestConflictResolver { conflict ->
            count += 1

            val timestamp = Clock.System.now().toString()
            val mDoc = MutableDocument("doc2", mapOf("timestamp" to timestamp))
            assertNotNull(mDoc)
            assertTrue(baseTestDb.save(mDoc, ConcurrencyControl.FAIL_ON_CONFLICT))

            val doc2 = baseTestDb.getDocument("doc2")
            assertNotNull(doc2)
            assertEquals(timestamp, doc2.getString("timestamp"))
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)

        assertEquals(1, count) // make sure, it entered the conflict resolver
    }

    @Test
    fun testNonBlockingConflictResolver() = runBlocking {
        val mutex = Mutex(true)
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        makeConflict("doc1", localData, remoteData)
        makeConflict("doc2", localData, remoteData)

        val config = getConfig(ReplicatorType.PULL)
        val order = mutableListOf<String>()
        val lock = reentrantLock()
        val resolver = TestConflictResolver { conflict ->
            // concurrent conflict resolver queue can cause race here
            lock.lock()
            order.add(conflict.documentId)
            val count = order.size
            lock.unlock()

            if (count == 1) {
                runBlocking { delay(0.5.seconds) }
            }

            order.add(conflict.documentId)
            if (order.size == 4) {
                mutex.unlock()
            }

            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        run(config)

        withTimeout(5.seconds) {
            mutex.lock()
        }

        // make sure, first doc starts resolution but finishes last.
        // in between second doc starts and finishes it.
        assertEquals(order.last(), order.first())
        assertEquals(order[2], order[1])
    }

    @Test
    fun testConflictResolverWhenDocumentIsPurged() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        val config = getConfig(ReplicatorType.PULL)

        makeConflict(docID, localData, remoteData)
        val resolver = TestConflictResolver { conflict ->
            baseTestDb.purge(conflict.documentId)
            conflict.remoteDocument
        }
        config.conflictResolver = resolver
        var error: CouchbaseLiteException? = null
        lateinit var replicator: Replicator
        lateinit var token: ListenerToken
        run(config) { repl ->
            replicator = repl
            token = repl.addDocumentReplicationListener { docRepl ->
                val err = docRepl.documents.firstOrNull()?.error
                if (err != null) {
                    error = err
                }
            }
        }
        assertNotNull(error)
        assertEquals(CBLError.Code.NOT_FOUND, error?.code)
        replicator.removeChangeListener(token)
    }
}
