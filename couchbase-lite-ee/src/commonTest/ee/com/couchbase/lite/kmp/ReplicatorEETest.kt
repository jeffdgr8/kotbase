package com.couchbase.lite.kmp

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
        println("1")
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        baseTestDb.save(doc1)

        println("2")
        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "striped")
        baseTestDb.save(doc2)

        println("3")
        // Push:
        val target = DatabaseEndpoint(otherDB)
        var config = makeConfig(target, ReplicatorType.PUSH, true)
        run(config)

        println("4")
        // Pull:
        config = makeConfig(target, ReplicatorType.PULL, true)
        run(config)

        assertEquals(2, baseTestDb.count)

        println("5")
        var doc = baseTestDb.getDocument("doc1")!!
        baseTestDb.purge(doc)

        println("6")
        doc = baseTestDb.getDocument("doc2")!!
        baseTestDb.purge(doc)

        println("7")
        assertEquals(0, baseTestDb.count)

        println("8")
        // Pull again, shouldn't have any new changes:
        run(config)
        assertEquals(0, baseTestDb.count)

        println("9")
        // Reset and pull:
        run(config, reset = true)
        println("10")
        assertEquals(2, baseTestDb.count)
        println("11")
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
        assertEquals(CBLError.Domain.CBLITE, err.getDomain())
        assertEquals(CBLError.Code.HTTP_CONFLICT, err.getCode())
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

        val token = replicator.addChangeListener { change ->
            val pDocIds = change.replicator.getPendingDocumentIds()

            if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
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

        val token = replicator.addChangeListener { change ->
            if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
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

        var pullOnlyError: CouchbaseLiteException? = null
        val token = replicator.addChangeListener { change ->
            // TODO: native C does not receive ReplicatorActivityLevel.CONNECTING
            // checking for != ReplicatorActivityLevel.STOPPED as workaround
            if (change.status.activityLevel != ReplicatorActivityLevel.STOPPED) {
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

        assertEquals(CBLError.Code.UNSUPPORTED, pullOnlyError?.getCode())
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

        var pullOnlyError: CouchbaseLiteException? = null
        val token = replicator.addChangeListener { change ->
            if (change.status.activityLevel == ReplicatorActivityLevel.CONNECTING) {
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

        assertEquals(CBLError.Code.UNSUPPORTED, pullOnlyError?.getCode())
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
}
