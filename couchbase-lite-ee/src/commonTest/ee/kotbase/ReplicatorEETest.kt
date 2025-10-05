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

import kotbase.logging.CustomLogSink
import kotbase.logging.LogSink
import kotbase.logging.LogSinks
import kotbase.test.IgnoreLinuxMingw
import kotbase.test.lockWithTimeout
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class ReplicatorEETest : BaseReplicatorTest() {

    // ReplicatorTest.swift

    class TestConflictResolver(
        // set this resolver, which will be used while resolving the conflict
        val resolver: ConflictResolver
    ) : ConflictResolver {

        var winner: Document? = null

        override fun invoke(conflict: Conflict): Document? {
            winner = resolver(conflict)
            return winner
        }
    }

    @Test
    fun testEmptyPush() {
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)
        config.run()
    }

    @Test
    fun testStartWithCheckpoint() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        testCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "striped")
        testCollection.save(doc2)

        // Push:
        val target = DatabaseEndpoint(targetDatabase)
        var config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)
        config.run()

        // Pull:
        config = makeSimpleReplConfig(target, type = ReplicatorType.PULL, continuous = false)
        config.run()

        assertEquals(2, testCollection.count)

        var doc = testCollection.getDocument("doc1")!!
        testCollection.purge(doc)

        doc = testCollection.getDocument("doc2")!!
        testCollection.purge(doc)

        assertEquals(0, testCollection.count)

        // Pull again, shouldn't have any new changes:
        config.run()
        assertEquals(0, testCollection.count)

        // Reset and pull:
        config.run(reset = true)
        assertEquals(2, testCollection.count)
    }

    @Test
    fun testStartWithResetCheckpointContinuous() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        testCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "striped")
        testCollection.save(doc2)

        // Push:
        val target = DatabaseEndpoint(targetDatabase)
        var config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = true)
        config.run()

        // Pull:
        config = makeSimpleReplConfig(target, type = ReplicatorType.PULL, continuous = true)
        config.run()

        assertEquals(2, testCollection.count)

        var doc = testCollection.getDocument("doc1")!!
        testCollection.purge(doc)

        doc = testCollection.getDocument("doc2")!!
        testCollection.purge(doc)

        assertEquals(0, testCollection.count)

        // Pull again, shouldn't have any new changes:
        config.run()
        assertEquals(0, testCollection.count)

        // Reset and pull:
        config.run(reset = true)
        assertEquals(2, testCollection.count)
    }

    @Test
    fun testDocumentReplicationEvent() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        testCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "Striped")
        testCollection.save(doc2)

        // Push:
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)

        val replicator = config.testReplicator()
        val docs = mutableListOf<ReplicatedDocument>()
        val token = replicator.addDocumentReplicationListener { replication ->
            assertTrue(replication.isPush)
            docs.addAll(replication.documents)
        }
        replicator.run()

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
        testCollection.save(doc3)

        // Run the replicator again:
        replicator.run()

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
        testCollection.save(doc4)

        // Remove document replication listener:
        token.remove()

        // Run the replicator again:
        replicator.run()

        // Should not be getting a new document replication event:
        assertEquals(3, docs.size)
    }

    @Test
    fun testDocumentReplicationEventWithPushConflict() {
        val doc1a = MutableDocument("doc1")
        doc1a.setString("species", "Tiger")
        doc1a.setString("pattern", "Star")
        testCollection.save(doc1a)

        val doc1b = MutableDocument("doc1")
        doc1b.setString("species", "Tiger")
        doc1b.setString("pattern", "Striped")
        targetCollection.save(doc1b)

        // Push:
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)

        val replicator = config.testReplicator()
        val docs = mutableListOf<ReplicatedDocument>()
        val token = replicator.addDocumentReplicationListener { replication ->
            assertTrue(replication.isPush)
            docs.addAll(replication.documents)
        }
        replicator.run()

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
        token.remove()
    }

    @Test
    fun testDocumentReplicationEventWithPullConflict() {
        val doc1a = MutableDocument("doc1")
        doc1a.setString("species", "Tiger")
        doc1a.setString("pattern", "Star")
        testCollection.save(doc1a)

        val doc1b = MutableDocument("doc1")
        doc1b.setString("species", "Tiger")
        doc1b.setString("pattern", "Striped")
        targetCollection.save(doc1b)

        // Pull:
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PULL, continuous = false)

        val replicator = config.testReplicator()
        val docs = mutableListOf<ReplicatedDocument>()
        val token = replicator.addDocumentReplicationListener { replication ->
            assertFalse(replication.isPush)
            docs.addAll(replication.documents)
        }
        replicator.run()

        // Check:
        assertEquals(1, docs.size)
        assertEquals("doc1", docs[0].id)
        assertNull(docs[0].error)
        assertFalse(docs[0].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[0].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Remove document replication listener:
        token.remove()
    }

    @Test
    fun testDocumentReplicationEventWithDeletion() {
        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Star")
        testCollection.save(doc1)

        // Delete:
        testCollection.delete(doc1)

        // Push:
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)

        val replicator = config.testReplicator()
        val docs = mutableListOf<ReplicatedDocument>()
        val token = replicator.addDocumentReplicationListener { replication ->
            assertTrue(replication.isPush)
            docs.addAll(replication.documents)
        }
        replicator.run()

        // Check:
        assertEquals(1, docs.size)
        assertEquals("doc1", docs[0].id)
        assertNull(docs[0].error)
        assertTrue(docs[0].flags.contains(DocumentFlag.DELETED))
        assertFalse(docs[0].flags.contains(DocumentFlag.ACCESS_REMOVED))

        // Remove document replication listener:
        token.remove()
    }

    @Test
    fun testSingleShotPushFilter() {
        testPushFilter(false)
    }

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
        testCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "Striped")
        doc2.setBlob("photo", blob)
        testCollection.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("species", "Tiger")
        doc3.setString("pattern", "Star")
        doc3.setBlob("photo", blob)
        testCollection.save(doc3)
        testCollection.delete(doc3)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pushFilter = { doc, flags ->
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
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PUSH, continuous = isContinuous)

        // Run the replicator:
        config.run()

        // Check documents passed to the filter:
        assertEquals(3, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("doc2"))
        assertTrue(docIds.contains("doc3"))

        // Check replicated documents:
        assertNotNull(targetCollection.getDocument("doc1"))
        assertNull(targetCollection.getDocument("doc2"))
        assertNull(targetCollection.getDocument("doc3"))
    }

    // TODO: native C fails sometimes
    //  AssertionError: Expected <3>, actual <2>.
    @IgnoreLinuxMingw
    @Test
    fun testPullFilter() {
        // Add a document to db database so that it can pull the deleted docs from:
        val doc0 = MutableDocument("doc0")
        doc0.setString("species", "Cat")
        testCollection.save(doc0)

        // Create documents:
        val content = "I'm a tiger.".encodeToByteArray()
        val blob = Blob("text/plain", content)

        val doc1 = MutableDocument("doc1")
        doc1.setString("species", "Tiger")
        doc1.setString("pattern", "Hobbes")
        doc1.setBlob("photo", blob)
        targetCollection.save(doc1)

        val doc2 = MutableDocument("doc2")
        doc2.setString("species", "Tiger")
        doc2.setString("pattern", "Striped")
        doc2.setBlob("photo", blob)
        targetCollection.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("species", "Tiger")
        doc3.setString("pattern", "Star")
        doc3.setBlob("photo", blob)
        targetCollection.save(doc3)
        targetCollection.delete(doc3)

        // Create replicator with pull filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pullFilter = { doc, flags ->
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
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PULL, continuous = false)

        // Run the replicator:
        config.run()

        // Check documents passed to the filter:
        assertEquals(3, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("doc2"))
        assertTrue(docIds.contains("doc3"))

        // Check replicated documents:
        assertNotNull(testCollection.getDocument("doc1"))
        assertNull(testCollection.getDocument("doc2"))
        assertNull(testCollection.getDocument("doc3"))
    }

    @Test
    fun testPushAndForget() = runBlocking {
        val doc = MutableDocument("doc1")
        doc.setString("species", "Tiger")
        doc.setString("pattern", "Hobbes")
        testCollection.save(doc)

        val mutex = Mutex(true)
        val docChangeToken = testCollection.addDocumentChangeListener(doc.id) { change ->
            assertEquals(change.documentID, doc.id)
            if (testCollection.getDocument(doc.id) == null) {
                mutex.unlock()
            }
        }
        assertEquals(1, testCollection.count)
        assertEquals(0, targetCollection.count)

        // Push:
        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)
        val replicator = config.testReplicator()
        val docReplicationToken = replicator.addDocumentReplicationListener {
            testCollection.setDocumentExpiration(doc.id, Clock.System.now())
        }
        replicator.run()

        assertTrue(mutex.lockWithTimeout(5.seconds))

        docReplicationToken.remove()
        docChangeToken.remove()

        assertEquals(0, testCollection.count)
        assertEquals(1, targetCollection.count)
    }

    // Removed Doc with Filter

    // TODO: native C fails sometimes
    //  AssertionError: Expected <2>, actual <1>.
    @IgnoreLinuxMingw
    @Test
    fun testPullRemovedDocWithFilterSingleShot() {
        testPullRemovedDocWithFilter(false)
    }

    // TODO: native C fails sometimes
    //  AssertionError: Expected <2>, actual <1>.
    @IgnoreLinuxMingw
    @Test
    fun testPullRemovedDocWithFilterContinuous() {
        testPullRemovedDocWithFilter(true)
    }

    private fun testPullRemovedDocWithFilter(isContinuous: Boolean) {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        targetCollection.save(doc1)

        val doc2 = MutableDocument("pass")
        doc2.setString("name", "pass")
        targetCollection.save(doc2)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pullFilter = { doc, flags ->
                assertNotNull(doc.id)

                val isAccessRemoved = flags.contains(DocumentFlag.ACCESS_REMOVED)
                if (isAccessRemoved) {
                    docIds.add(doc.id)
                    doc.id == "pass"
                } else {
                    doc.getString("name") == "pass"
                }
            }
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PULL, continuous = isContinuous)

        // Run the replicator:
        config.run()
        assertEquals(0, docIds.size)

        assertNotNull(testCollection.getDocument("doc1"))
        assertNotNull(testCollection.getDocument("pass"))

        val doc1Mutable = targetCollection.getDocument("doc1")?.toMutable() ?: error("Docs must exists")
        doc1Mutable.setData(mapOf("_removed" to true))
        targetCollection.save(doc1Mutable)

        val doc2Mutable = targetCollection.getDocument("pass")?.toMutable() ?: error("Docs must exists")
        doc2Mutable.setData(mapOf("_removed" to true))
        targetCollection.save(doc2Mutable)

        config.run()

        // Check documents passed to the filter:
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("pass"))

        assertNotNull(testCollection.getDocument("doc1"))
        assertNull(testCollection.getDocument("pass"))
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

    // TODO: native C fails sometimes
    //  AssertionError: Expected <2>, actual <1>.
    @IgnoreLinuxMingw
    @Test
    fun testPullDeletedDocWithFilterSingleShot() {
        testPullDeletedDocWithFilter(false)
    }

    // TODO: native C fails sometimes
    //  AssertionError: Expected <2>, actual <1>.
    //  or AssertionError: Expected value to be true.
    @IgnoreLinuxMingw
    @Test
    fun testPullDeletedDocWithFilterContinuous() {
        testPullDeletedDocWithFilter(true)
    }

    private fun testPushDeletedDocWithFilter(isContinuous: Boolean) {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        testCollection.save(doc1)

        val doc2 = MutableDocument("pass")
        doc2.setString("name", "pass")
        testCollection.save(doc2)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pushFilter = { doc, flags ->
                assertNotNull(doc.id)

                val isDeleted = flags.contains(DocumentFlag.DELETED)
                if (isDeleted) {
                    docIds.add(doc.id)
                    doc.id == "pass"
                } else {
                    doc.getString("name") == "pass"
                }
            }
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PUSH, continuous = isContinuous)

        // Run the replicator:
        config.run()
        assertEquals(0, docIds.size)

        assertNotNull(targetCollection.getDocument("doc1"))
        assertNotNull(targetCollection.getDocument("pass"))

        testCollection.delete(doc1)
        testCollection.delete(doc2)

        config.run()

        // Check documents passed to the filter:
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("pass"))

        assertNotNull(targetCollection.getDocument("doc1"))
        assertNull(targetCollection.getDocument("pass"))
    }

    private fun testPullDeletedDocWithFilter(isContinuous: Boolean) {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        targetCollection.save(doc1)

        val doc2 = MutableDocument("pass")
        doc2.setString("name", "pass")
        targetCollection.save(doc2)

        // Create replicator with push filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pullFilter = { doc, flags ->
                assertNotNull(doc.id)

                val isDeleted = flags.contains(DocumentFlag.DELETED)
                if (isDeleted) {
                    docIds.add(doc.id)
                    doc.id == "pass"
                } else {
                    doc.getString("name") == "pass"
                }
            }
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PULL, continuous = isContinuous)

        // Run the replicator:
        config.run()
        assertEquals(0, docIds.size)

        assertNotNull(testCollection.getDocument("doc1"))
        assertNotNull(testCollection.getDocument("pass"))

        targetCollection.delete(doc1)
        targetCollection.delete(doc2)

        config.run()

        // Check documents passed to the filter:
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc1"))
        assertTrue(docIds.contains("pass"))

        assertNotNull(testCollection.getDocument("doc1"))
        assertNull(testCollection.getDocument("pass"))
    }

    // stop and restart replication with filter

    @Test
    fun testStopAndRestartPushReplicationWithFilter() {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        testCollection.save(doc1)

        // Create replicator with pull filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pushFilter = { doc, _ ->
                assertNotNull(doc.id)
                docIds.add(doc.id)
                doc.getString("name") == "pass"
            }
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PUSH, continuous = true)

        // create a replicator
        val repl = config.testReplicator()
        repl.run()

        assertEquals(1, docIds.size)
        assertEquals(1, targetCollection.count)
        assertEquals(1, testCollection.count)

        // make some more changes
        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "pass")
        testCollection.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("name", "donotpass")
        testCollection.save(doc3)

        // restart the same replicator
        docIds.clear()
        repl.run()

        // should use the same replicator filter.
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc3"))
        assertTrue(docIds.contains("doc2"))

        assertNotNull(targetCollection.getDocument("doc1"))
        assertNotNull(targetCollection.getDocument("doc2"))
        assertNull(targetCollection.getDocument("doc3"))
        assertEquals(3, testCollection.count)
        assertEquals(2, targetCollection.count)
    }

    // TODO: native C fails sometimes
    //  AssertionError: Expected <2>, actual <1>.
    @IgnoreLinuxMingw
    @Test
    fun testStopAndRestartPullReplicationWithFilter() {
        // Create documents:
        val doc1 = MutableDocument("doc1")
        doc1.setString("name", "pass")
        targetCollection.save(doc1)

        // Create replicator with pull filter:
        val docIds = mutableSetOf<String>()
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = CollectionConfiguration(
            pullFilter = { doc, _ ->
                assertNotNull(doc.id)
                docIds.add(doc.id)
                doc.getString("name") == "pass"
            }
        )
        val config = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PULL, continuous = true)

        // create a replicator
        val repl = config.testReplicator()
        repl.run()

        assertEquals(1, docIds.size)
        assertEquals(1, targetCollection.count)
        assertEquals(1, testCollection.count)

        // make some more changes
        val doc2 = MutableDocument("doc2")
        doc2.setString("name", "pass")
        targetCollection.save(doc2)

        val doc3 = MutableDocument("doc3")
        doc3.setString("name", "donotpass")
        targetCollection.save(doc3)

        // restart the same replicator
        docIds.clear()
        repl.run()

        // should use the same replicator filter.
        assertEquals(2, docIds.size)
        assertTrue(docIds.contains("doc3"))
        assertTrue(docIds.contains("doc2"))

        assertNotNull(testCollection.getDocument("doc1"))
        assertNotNull(testCollection.getDocument("doc2"))
        assertNull(testCollection.getDocument("doc3"))
        assertEquals(3, targetCollection.count)
        assertEquals(2, testCollection.count)
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
        for (i in 0..<noOfDocument) {
            val doc = MutableDocument("doc-$i")
            doc.setValue(kActionKey, kCreateActionValue)
            saveDocInCollection(doc)
            docIds.add("doc-$i")
        }
        return docIds
    }

    private fun validatePendingDocumentIDs(docIds: Set<String>, pushOnlyDocIds: Set<String>? = null) {
        val colConfig = CollectionConfiguration()
        if (pushOnlyDocIds?.isNotEmpty() == true) {
            colConfig.pushFilter = { doc, _ ->
                pushOnlyDocIds.contains(doc.id)
            }
        }
        val replConfig = makeSimpleReplConfig(DatabaseEndpoint(targetDatabase), srcConfig = colConfig, type = ReplicatorType.PUSH, continuous = false)
        val replicator = replConfig.testReplicator()

        // Check document pending:
        var pendingIds = replicator.getPendingDocumentIds(testCollection)

        if (pushOnlyDocIds?.isNotEmpty() == true) {
            assertEquals(pendingIds.size, pushOnlyDocIds.size)
        } else {
            assertEquals(pendingIds.size, docIds.size)
        }

        for (docId in docIds) {
            val willBePush = pushOnlyDocIds?.contains(docId) != false
            if (willBePush) {
                assertTrue(pendingIds.contains(docId))
                assertTrue(replicator.isDocumentPending(docId, testCollection))
            }
        }

        // Run replicator:
        replicator.run()

        // Check document pending:
        pendingIds = replicator.getPendingDocumentIds(testCollection)
        assertEquals(0, pendingIds.size)

        for (docId in docIds) {
            assertFalse(replicator.isDocumentPending(docId, testCollection))
        }
    }

    // Unit Tests

    @Test
    fun testPendingDocIDsPullOnlyException() = runBlocking {
        val target = DatabaseEndpoint(targetDatabase)
        val replConfig = makeSimpleReplConfig(target, type = ReplicatorType.PULL, continuous = false)
        val replicator = replConfig.testReplicator()

        var pullOnlyError: CouchbaseLiteException? = null
        try {
            replicator.getPendingDocumentIds(testCollection)
        } catch (e: CouchbaseLiteException) {
            pullOnlyError = e
        }

        assertEquals(CBLError.Code.UNSUPPORTED, pullOnlyError?.code)
    }

    @Test
    fun testPendingDocIDsWithCreate() {
        val docIds = createDocs()
        validatePendingDocumentIDs(docIds)
    }

    @Test
    fun testPendingDocIDsWithUpdate() {
        createDocs()

        val target = DatabaseEndpoint(targetDatabase)
        val replConfig = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)
        replConfig.run()

        val updatedIds = setOf("doc-2", "doc-4")
        for (docId in updatedIds) {
            val doc = testCollection.getDocument(docId)!!.toMutable()
            doc.setString(kActionKey, kUpdateActionValue)
            saveDocInCollection(doc)
        }

        validatePendingDocumentIDs(updatedIds)
    }

    @Test
    fun testPendingDocIdsWithDelete() {
        createDocs()

        val target = DatabaseEndpoint(targetDatabase)
        val replConfig = makeSimpleReplConfig(target, type = ReplicatorType.PUSH, continuous = false)
        replConfig.run()

        val deletedIds = setOf("doc-2", "doc-4")
        for (docId in deletedIds) {
            val doc = testCollection.getDocument(docId)!!
            testCollection.delete(doc)
        }

        validatePendingDocumentIDs(deletedIds)
    }

    @Test
    fun testPendingDocIdsWithPurge() {
        val docs = createDocs()

        testCollection.purge("doc-3")
        docs.remove("doc-3")

        validatePendingDocumentIDs(docs)
    }

    @Test
    fun testPendingDocIdsWithFilter() {
        val docIds = createDocs()

        val pushOnlyIds = setOf("doc-2", "doc-4")
        validatePendingDocumentIDs(docIds, pushOnlyIds)
    }

    // ReplicatorTest+CustomConflict.swift

    @IgnoreLinuxMingw
    @Test
    fun testConflictResolverConfigProperty() {
        val target = URLEndpoint("wss://foo/db")

        val colConfig = CollectionConfiguration(
            conflictResolver = { conflict ->
                conflict.remoteDocument
            }
        )
        val pullConfig = makeSimpleReplConfig(target, srcConfig = colConfig, type = ReplicatorType.PULL, continuous = false)
        val repl = pullConfig.testReplicator()

        assertNotNull(pullConfig.getCollectionConfiguration(testCollection)!!.conflictResolver)
        assertNotNull(repl.config.getCollectionConfiguration(testCollection)!!.conflictResolver)
    }

    private fun getConfig(type: ReplicatorType, conflictResolver: ConflictResolver? = null): ReplicatorConfiguration {
        val target = DatabaseEndpoint(targetDatabase)
        val colConfig = conflictResolver?.let { CollectionConfiguration(conflictResolver = it) }
        return makeSimpleReplConfig(target, srcConfig = colConfig, type = type, continuous = false)
    }

    private fun makeConflict(
        docID: String,
        localData: Map<String, Any?>?,
        remoteData: Map<String, Any?>?
    ) {
        // create doc
        val doc = MutableDocument(docID)
        saveDocInCollection(doc)

        // sync the doc in both DBs.
        val config = getConfig(ReplicatorType.PUSH)
        config.run()

        // Now make different changes in db and oDBs
        if (localData != null) {
            val doc1a = testCollection.getDocument(docID)!!.toMutable()
            doc1a.setData(localData)
            saveDocInCollection(doc1a)
        } else {
            testCollection.delete(testCollection.getDocument(docID)!!)
        }

        if (remoteData != null) {
            val doc1b = targetCollection.getDocument(docID)!!.toMutable()
            doc1b.setData(remoteData)
            targetCollection.save(doc1b)
        } else {
            targetCollection.delete(targetCollection.getDocument(docID)!!)
        }
    }

    @Test
    fun testConflictResolverRemoteWins() {
        val localData = mapOf("name" to "Hobbes")
        val remoteData = mapOf("pattern" to "striped")
        makeConflict("doc", localData, remoteData)

        val resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertEquals(1, testCollection.count)
        assertEquals(testCollection.getDocument("doc")!!, resolver.winner!!)
        assertEquals(remoteData, testCollection.getDocument("doc")!!.toMap())
    }

    @Test
    fun testConflictResolverLocalWins() {
        val localData = mapOf("name" to "Hobbes")
        val remoteData = mapOf("pattern" to "striped")
        makeConflict("doc", localData, remoteData)

        val resolver = TestConflictResolver { conflict ->
            conflict.localDocument
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertEquals(1, testCollection.count)
        assertEquals(testCollection.getDocument("doc")!!, resolver.winner!!)
        assertEquals(localData, testCollection.getDocument("doc")!!.toMap())
    }

    @Test
    fun testConflictResolverNullDoc() {
        val localData = mapOf("name" to "Hobbes")
        val remoteData = mapOf("pattern" to "striped")
        makeConflict("doc", localData, remoteData)

        val resolver = TestConflictResolver {
            null
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertNull(resolver.winner)
        assertEquals(0, testCollection.count)
        assertNull(testCollection.getDocument("doc"))
    }

    /**
     * https://github.com/couchbaselabs/couchbase-lite-api/blob/master/spec/tests/T0005-Version-Vector.md
     * Test 4. DefaultConflictResolverDeleteWins -> testConflictResolverDeletedLocalWins + testConflictResolverDeletedRemoteWins
     */
    @Test
    fun testConflictResolverDeletedLocalWins() {
        val remoteData = mapOf("key2" to "value2")
        makeConflict("doc", null, remoteData)

        val resolver = TestConflictResolver { conflict ->
            assertNull(conflict.localDocument)
            assertNotNull(conflict.remoteDocument)
            null
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertNull(resolver.winner)
        assertEquals(0, testCollection.count)
        assertNull(testCollection.getDocument("doc"))
    }

    @Test
    fun testConflictResolverDeletedRemoteWins() {
        val localData = mapOf("key1" to "value1")
        makeConflict("doc", localData, null)

        val resolver = TestConflictResolver { conflict ->
            assertNotNull(conflict.localDocument)
            assertNull(conflict.remoteDocument)
            null
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertNull(resolver.winner)
        assertEquals(0, testCollection.count)
        assertNull(testCollection.getDocument("doc"))
    }

    // TODO: native C fails
    //  AssertionError: Expected <2>, actual <1>.
    @IgnoreLinuxMingw
    @Test
    fun testConflictResolverCalledTwice() {
        val docID = "doc"
        val localData = mapOf<String, Any?>("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        makeConflict(docID, localData, remoteData)
        var count = 0
        val resolver = TestConflictResolver { conflict ->
            count += 1

            // update the doc will cause a second conflict
            val savedDoc = testCollection.getDocument(docID)!!.toMutable()
            if (!savedDoc["secondUpdate"].exists) {
                savedDoc.setBoolean("secondUpdate", true)
                testCollection.save(savedDoc)
            }

            val mDoc = conflict.localDocument!!.toMutable()
            mDoc.setString("edit", "local")
            mDoc
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertEquals(2, count)
        assertEquals(1, testCollection.count)
        val expectedDocDict = localData.toMutableMap()
        expectedDocDict["edit"] = "local"
        expectedDocDict["secondUpdate"] = true
        assertEquals(expectedDocDict, testCollection.getDocument(docID)!!.toMap())
    }

    @Test
    fun testConflictResolverMergeDoc() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        // EDIT LOCAL DOCUMENT
        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver { conflict ->
            val doc = conflict.localDocument?.toMutable()
            doc?.setString("edit", "local")
            doc
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        var expectedDocDict = localData.toMutableMap()
        expectedDocDict["edit"] = "local"
        var value = testCollection.getDocument(docID)!!.toMap()
        assertEquals(expectedDocDict, value)

        // EDIT REMOTE DOCUMENT
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            val doc = conflict.remoteDocument?.toMutable()
            doc?.setString("edit", "remote")
            doc
        }
        var colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)
        config.run()

        expectedDocDict = remoteData.toMutableMap()
        expectedDocDict["edit"] = "remote"
        assertEquals(expectedDocDict, testCollection.getDocument(docID)!!.toMap())

        // CREATE NEW DOCUMENT
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            val doc = MutableDocument(conflict.localDocument!!.id)
            doc.setString("docType", "new-with-same-ID")
            doc
        }
        colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)
        config.run()

        value = testCollection.getDocument(docID)!!.toMap()
        assertEquals(mapOf("docType" to "new-with-same-ID"), value)
    }

    @Test
    fun testDocumentReplicationEventForConflictedDocs() {
        // when resolution is skipped: here doc from oDB throws an exception & skips it
        var resolver = TestConflictResolver {
            targetCollection.getDocument("doc")
        }
        try {
            validateDocumentReplicationEventForConflictedDocs(resolver)
        } catch (_: Exception) {}

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
        val config = getConfig(ReplicatorType.PULL, resolver)

        makeConflict(docID, localData, remoteData)

        val replicator = config.testReplicator()
        val docIds = mutableListOf<String>()
        val token = replicator.addDocumentReplicationListener { docRepl ->
            for (doc in docRepl.documents) {
                docIds.add(doc.id)
            }
        }
        replicator.run()

        // make sure only single listener event is fired when conflict occurred.
        assertEquals(1, docIds.size)
        assertEquals(docID, docIds.first())
        token.remove()

        // resolve any un-resolved conflict through pull replication.
        getConfig(ReplicatorType.PULL).run()
    }

    class TestCustomLogSink: LogSink {
        var lines = mutableListOf<String>()

        override fun writeLog(level: LogLevel, domain: LogDomain, message: String) {
            lines.add(message)
        }

        fun reset() {
            lines.clear()
        }

        fun containsString(string: String): Boolean {
            for (line in lines) {
                if (line.contains(string)) {
                    return true
                }
            }
            return false
        }
    }

    @Test
    fun testConflictResolverWrongDocID() {
        // use this to verify the logs generated during the conflict resolution.
        val customLogger = TestCustomLogSink()
        LogSinks.custom = CustomLogSink(level = LogLevel.WARNING, logSink = customLogger)

        val docID = "doc"
        val wrongDocID = "wrong-doc-id"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        makeConflict(docID, localData, remoteData)
        val resolver = TestConflictResolver {
            val mDoc = MutableDocument(wrongDocID)
            mDoc.setString("edit", "update")
            mDoc
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        val replicator = config.testReplicator()
        val docIds = mutableSetOf<String>()
        val token = replicator.addDocumentReplicationListener { docRepl ->
            if (docRepl.documents.isNotEmpty()) {
                assertEquals(1, docRepl.documents.size)
                docIds.add(docRepl.documents.first().id)
            }

            // shouldn't report an error from replicator
            assertNull(docRepl.documents.firstOrNull()?.error)
        }
        replicator.run()
        token.remove()

        // validate wrong doc-id is resolved successfully
        assertEquals(1, testCollection.count)
        assertTrue(docIds.contains(docID))
        assertEquals(mapOf("edit" to "update"), testCollection.getDocument(docID)!!.toMap())

        // validate the warning log
        assertTrue(
            customLogger.lines.contains( // iOS log
                "The document ID of the resolved document '$wrongDocID' " +
                        "is not matching with the document ID of the conflicting " +
                        "document '$docID'."
            ) || customLogger.lines.contains( // Java log
                "[JAVA] Conflict resolution for a document produced a new document " +
                        "with ID '$wrongDocID', which does not match " +
                        "the IDs of the conflicting document ($docID)"
            ) || customLogger.lines.contains( // Native C log
                "The document ID '$wrongDocID' of the resolved document is not " +
                        "matching with the document ID '$docID' of the conflicting document."
            )
        )

        LogSinks.custom = null
        customLogger.reset()
    }

    @Test
    fun testConflictResolverDifferentDBDoc() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver {
            targetCollection.getDocument(docID) // doc from different DB!!
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        val replicator = config.testReplicator()
        var error: CouchbaseLiteException? = null
        val token = replicator.addDocumentReplicationListener { docRepl ->
            val err = docRepl.documents.firstOrNull()?.error
            if (err != null) {
                error = err
            }
        }

        try {
            replicator.run()
        } catch (_: Exception) {}
        assertNotNull(error)
        assertTrue(
            error.code == CBLError.Code.UNEXPECTED_ERROR || // Java uses this code
                    error.code == CBLError.Code.CONFLICT || // iOS uses this code
                    error.code == CBLError.Code.INVALID_PARAMETER // Native C uses this code
        )
        assertEquals(CBLError.Domain.CBLITE, error.domain)

        token.remove()
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        val colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)
        config.run()
        assertEquals(remoteData, testCollection.getDocument(docID)!!.toMap())
    }

    /// disabling since, exceptions inside conflict handler will leak, since objc doesn't perform release
    /// when exception happens
    // TODO: Kotlin Exception without @Throws(), which resolve() interface lacks,
    //  and NSException both unable to be forwarded to Objective-C or Native C caller
    // TODO: on JVM/Android error.code != CONFLICT, error.code == UNEXPECTED_ERROR
    @Ignore
    @Test
    fun testConflictResolverThrowingException() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver {
            throw IllegalStateException("some exception happened inside custom conflict resolution")
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        val replicator = config.testReplicator()
        var error: CouchbaseLiteException? = null
        val token = replicator.addDocumentReplicationListener { docRepl ->
            val err = docRepl.documents.firstOrNull()?.error
            if (err != null) {
                error = err
            }
        }

        try {
            replicator.run()
        } catch (_: Exception) {}

        assertNotNull(error)
        assertEquals(CBLError.Code.CONFLICT, error.code)
        assertEquals(CBLError.Domain.CBLITE, error.domain)
        token.remove()
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        val colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)

        config.run()
        assertEquals(remoteData, testCollection.getDocument(docID)!!.toMap())
    }

    /**
     * https://github.com/couchbaselabs/couchbase-lite-api/blob/master/spec/tests/T0005-Version-Vector.md
     * Test 3. DefaultConflictResolverLastWriteWins -> default resolver
     */
    @Test
    fun testConflictResolutionDefault() {
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        // higher generation-id
        var docID = "doc1"
        makeConflict(docID, localData, remoteData)
        var doc = testCollection.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        saveDocInCollection(doc)

        // delete local
        docID = "doc2"
        makeConflict(docID, localData, remoteData)
        testCollection.delete(testCollection.getDocument(docID)!!)
        doc = targetCollection.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        targetCollection.save(doc)

        // delete remote
        docID = "doc3"
        makeConflict(docID, localData, remoteData)
        doc = testCollection.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        testCollection.save(doc)
        targetCollection.delete(targetCollection.getDocument(docID)!!)

        // delete local but higher remote generation
        docID = "doc4"
        makeConflict(docID, localData, remoteData)
        testCollection.delete(testCollection.getDocument(docID)!!)
        doc = targetCollection.getDocument(docID)!!.toMutable()
        doc.setString("key3", "value3")
        targetCollection.save(doc)
        doc = targetCollection.getDocument(docID)!!.toMutable()
        doc.setString("key4", "value4")
        targetCollection.save(doc)

        val config = getConfig(ReplicatorType.PULL, ReplicatorConfiguration.DEFAULT_CONFLICT_RESOLVER)
        config.run()

        // validate saved doc includes the key3, which is the highest generation.
        assertEquals("value3", testCollection.getDocument("doc1")?.getString("key3"))

        // validates the deleted doc is chosen for its counterpart doc which saved
        assertNull(testCollection.getDocument("doc2"))
        assertNull(testCollection.getDocument("doc3"))

        // validates the deleted doc is chosen without considering the generation.
        assertNull(testCollection.getDocument("doc4"))
    }

    @Test
    fun testConflictResolverReturningBlob() {
        val docID = "doc"
        val content = "I am a blob".encodeToByteArray()
        var blob = Blob("text/plain", content)


        // RESOLVE WITH REMOTE and BLOB data in LOCAL
        var localData = mapOf("key1" to "value1", "blob" to blob)
        var remoteData = mapOf<String, Any>("key2" to "value2")
        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        config.run()

        assertNull(testCollection.getDocument(docID)?.getBlob("blob"))
        assertEquals(remoteData, testCollection.getDocument(docID)!!.toMap())

        // RESOLVE WITH LOCAL with BLOB data
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            conflict.localDocument
        }
        var colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)

        config.run()

        assertEquals(blob, testCollection.getDocument(docID)?.getBlob("blob"))
        assertEquals("value1", testCollection.getDocument(docID)?.getString("key1"))

        // RESOLVE WITH LOCAL and BLOB data in REMOTE
        blob = Blob("text/plain", content)
        localData = mapOf("key1" to "value1")
        remoteData = mapOf("key2" to "value2", "blob" to blob)
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            conflict.localDocument
        }
        colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)

        config.run()

        assertNull(testCollection.getDocument(docID)?.getBlob("blob"))
        assertEquals(localData, testCollection.getDocument(docID)!!.toMap())

        // RESOLVE WITH REMOTE with BLOB data
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            conflict.remoteDocument
        }
        colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)

        config.run()

        assertEquals(blob, testCollection.getDocument(docID)?.getBlob("blob"))
        assertEquals("value2", testCollection.getDocument(docID)?.getString("key2"))
    }

    // TODO: native C fails
    //  AssertionError: Expected value to be not null.
    @IgnoreLinuxMingw
    @Test
    fun testConflictResolverReturningBlobFromDifferentDB() {
        val docID = "doc"
        val content = "I am a blob".encodeToByteArray()
        val blob = Blob("text/plain", content)
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2", "blob" to blob)

        // using remote document blob is okay to use!
        makeConflict(docID, localData, remoteData)
        var resolver = TestConflictResolver { conflict ->
            val mDoc = conflict.localDocument?.toMutable()
            mDoc?.setBlob("blob", conflict.remoteDocument?.getBlob("blob"))
            mDoc
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        var replicator = config.testReplicator()
        var token = replicator.addDocumentReplicationListener { docRepl ->
            assertNull(docRepl.documents.firstOrNull()?.error)
        }
        replicator.run()
        token.remove()

        // using blob from remote document of user's- which is a different database
        val oDBDoc = targetCollection.getDocument(docID)!!
        makeConflict(docID, localData, remoteData)
        resolver = TestConflictResolver { conflict ->
            val mDoc = conflict.localDocument?.toMutable()
            mDoc?.setBlob("blob", oDBDoc.getBlob("blob"))
            mDoc
        }
        val colConfig = CollectionConfiguration(conflictResolver = resolver)
        config.addCollection(testCollection, colConfig)
        replicator = config.testReplicator()
        var error: CouchbaseLiteException? = null
        token = replicator.addDocumentReplicationListener { docRepl ->
            val err = docRepl.documents.firstOrNull()?.error
            if (err != null) {
                error = err
            }
        }
        try {
            replicator.run()
        } catch (_: Exception) {}
        assertNotNull(error)
        assertEquals(CBLError.Code.UNEXPECTED_ERROR, error.code)
        assertTrue(
            error.message.contains(
                "A document contains a blob that was saved to a different " +
                        "database. The save operation cannot complete."
            )
        )
        token.remove()
    }

    @Test
    fun testNonBlockingDatabaseOperationConflictResolver() {
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        makeConflict("doc1", localData, remoteData)

        var count = 0
        val resolver = TestConflictResolver { conflict ->
            count += 1

            val timestamp = Clock.System.now().toString()
            val mDoc = MutableDocument("doc2", mapOf("timestamp" to timestamp))
            assertNotNull(mDoc)
            assertTrue(testCollection.save(mDoc, ConcurrencyControl.FAIL_ON_CONFLICT))

            val doc2 = testCollection.getDocument("doc2")
            assertNotNull(doc2)
            assertEquals(timestamp, doc2.getString("timestamp"))
            conflict.remoteDocument
        }
        val config = getConfig(ReplicatorType.PULL, resolver)

        config.run()

        assertEquals(1, count) // make sure, it entered the conflict resolver
    }

    // TODO: native C fails
    //  AssertionError: Expected <doc2>, actual <doc1>.
    @IgnoreLinuxMingw
    @Test
    fun testNonBlockingConflictResolver() = runBlocking {
        val mutex = Mutex(true)
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")
        makeConflict("doc1", localData, remoteData)
        makeConflict("doc2", localData, remoteData)

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
        val config = getConfig(ReplicatorType.PULL, resolver)

        config.run()

        assertTrue(mutex.lockWithTimeout(5.seconds))

        // make sure, first doc starts resolution but finishes last.
        // in between second doc starts and finishes it.
        assertEquals(order.last(), order.first())
        assertEquals(order[2], order[1])
    }

    // TODO: native C fails
    //  AssertionError: Expected value to be not null.
    @IgnoreLinuxMingw
    @Test
    fun testConflictResolverWhenDocumentIsPurged() {
        val docID = "doc"
        val localData = mapOf("key1" to "value1")
        val remoteData = mapOf("key2" to "value2")

        makeConflict(docID, localData, remoteData)
        val resolver = TestConflictResolver { conflict ->
            testCollection.purge(conflict.documentId)
            conflict.remoteDocument
        }
        val config = getConfig(ReplicatorType.PULL, resolver)
        var error: CouchbaseLiteException? = null
        val replicator = config.testReplicator()
        val token = replicator.addDocumentReplicationListener { docRepl ->
            val err = docRepl.documents.firstOrNull()?.error
            if (err != null) {
                error = err
            }
        }
        replicator.run()
        assertNotNull(error)
        assertEquals(CBLError.Code.NOT_FOUND, error.code)
        token.remove()
    }

    // ReplicatorTest+Collection.swift

    // MARK: 8.14 Replicator

    fun createDocNumbered(col: Collection, start: Int, num: Int) {
        for (i in start..<start+num) {
            val mdoc = MutableDocument("doc$i")
            mdoc.setInt("number1", i)
            col.save(mdoc)
        }
    }

    @Test
    fun testCollectionSingleShotPushReplication() {
        testCollectionPushReplication(continous = false)
    }

    @Test
    fun testCollectionContinuousPushReplication() {
        testCollectionPushReplication(continous = true)
    }

    private fun testCollectionPushReplication(continous: Boolean) {
        val totalDocs = 10

        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col1a, 0, totalDocs)
        createDocNumbered(col1b, 10, totalDocs)
        assertEquals(totalDocs.toLong(), col1a.count)
        assertEquals(totalDocs.toLong(), col1b.count)
        assertEquals(0, col2a.count)
        assertEquals(0, col2b.count)

        createDocNumbered(testCollection, 20, totalDocs)
        assertEquals(totalDocs.toLong(), testCollection.count)
        assertEquals(0, targetCollection.count)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a, col1b, testCollection), target = target, type = ReplicatorType.PUSH, continuous = continous)
        config.run()

        assertEquals(totalDocs.toLong(), col2a.count)
        assertEquals(totalDocs.toLong(), col2b.count)
        assertEquals(totalDocs.toLong(), col1a.count)
        assertEquals(totalDocs.toLong(), col1b.count)
        assertEquals(totalDocs.toLong(), testCollection.count)
        assertEquals(totalDocs.toLong(), targetCollection.count)
    }

    @Test
    fun testCollectionSingleShotPullReplication() {
        testCollectionPullReplication(continous = false)
    }

    @Test
    fun testCollectionContinuousPullReplication() {
        testCollectionPullReplication(continous = true)
    }

    private fun testCollectionPullReplication(continous: Boolean) {
        val totalDocs = 10

        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col2a, 0, totalDocs)
        createDocNumbered(col2b, 10, totalDocs)
        assertEquals(totalDocs.toLong(), col2a.count)
        assertEquals(totalDocs.toLong(), col2b.count)
        assertEquals(0, col1a.count)
        assertEquals(0, col1b.count)

        createDocNumbered(targetCollection, 20, totalDocs)
        assertEquals(totalDocs.toLong(), targetCollection.count)
        assertEquals(0, testCollection.count)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a, col1b, testCollection), target = target, type = ReplicatorType.PULL, continuous = continous)
        config.run()

        assertEquals(totalDocs.toLong(), col2a.count)
        assertEquals(totalDocs.toLong(), col2b.count)
        assertEquals(totalDocs.toLong(), col1a.count)
        assertEquals(totalDocs.toLong(), col1b.count)
        assertEquals(totalDocs.toLong(), testCollection.count)
        assertEquals(totalDocs.toLong(), targetCollection.count)
    }

    @Test
    fun testCollectionSingleShotPushPullReplication() {
        testCollectionPushPullReplication(continous = false)
    }

    @Test
    fun testCollectionContinuousPushPullReplication() {
        testCollectionPushPullReplication(continous = true)
    }

    private fun testCollectionPushPullReplication(continous: Boolean) {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col1a, 0, 2)
        createDocNumbered(col2a, 10, 5)

        createDocNumbered(col1b, 5, 3)
        createDocNumbered(col2b, 15, 8)

        assertEquals(2, col1a.count)
        assertEquals(5, col2a.count)

        assertEquals(3, col1b.count)
        assertEquals(8, col2b.count)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a, col1b), target = target, type = ReplicatorType.PUSH_AND_PULL, continuous = continous)
        config.run()

        assertEquals(7, col1a.count)
        assertEquals(7, col2a.count)
        assertEquals(11, col1b.count)
        assertEquals(11, col2b.count)
    }

    @Test
    fun testCollectionResetReplication() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col2a = targetDatabase.createCollection("colA", "scopeA")

        createDocNumbered(col2a, 0, 5)
        assertEquals(0, col1a.count)
        assertEquals(5, col2a.count)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a), target = target, type = ReplicatorType.PULL, continuous = false)
        config.run()

        assertEquals(5, col1a.count)
        assertEquals(5, col2a.count)

        // Purge all documents from the colA of the database A.
        for (i in 0..<5) {
            col1a.purge("doc$i")
        }
        config.run()

        assertEquals(0, col1a.count)
        assertEquals(5, col2a.count)

        config.run(reset = true)

        assertEquals(5, col1a.count)
        assertEquals(5, col2a.count)
    }

    @Test
    fun testCollectionDefaultConflictResolver() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col2a = targetDatabase.createCollection("colA", "scopeA")

        val doc1 = MutableDocument("doc1")
        col1a.save(doc1)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a), target = target, type = ReplicatorType.PUSH_AND_PULL, continuous = false)
        config.run()

        val mdoc1 = col1a.getDocument("doc1")!!.toMutable()
        mdoc1.setString("update", "update1")
        col1a.save(mdoc1)

        var mdoc2 = col2a.getDocument("doc1")!!.toMutable()
        mdoc2.setString("update", "update2.1")
        col2a.save(mdoc2)

        mdoc2 = col2a.getDocument("doc1")!!.toMutable()
        mdoc2.setString("update", "update2.2")
        col2a.save(mdoc2)

        val r = Replicator(config)
        var count = 0
        r.addDocumentReplicationListener { docReplication ->
            count += 1

            assertEquals(1, docReplication.documents.size)
            val doc = docReplication.documents[0]
            if (count == 1) {
                assertNull(doc.error)
            } else {
                assertEquals(CBLError.Code.HTTP_CONFLICT, doc.error?.code)
            }

        }
        r.run()

        val doc = col1a.getDocument("doc1")
        assertEquals("update2.2", doc!!.getString("update"))
    }

    @Test
    fun testCollectionConflictResolver() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        // Create a document with id "doc1" in colA and colB of the database A.
        val doc1a = MutableDocument("doc1")
        doc1a.setString("update", "update0")
        col1a.save(doc1a)

        val doc1b = MutableDocument("doc2")
        doc1b.setString("update", "update0")
        col1b.save(doc1b)

        val conflictResolver1 = TestConflictResolver { con: Conflict ->
            con.localDocument
        }
        val conflictResolver2 = TestConflictResolver { con: Conflict ->
            con.remoteDocument
        }

        val colConfig1 = CollectionConfiguration()
        colConfig1.conflictResolver = conflictResolver1

        val colConfig2 = CollectionConfiguration()
        colConfig2.conflictResolver = conflictResolver2

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeReplConfig(target = target, type = ReplicatorType.PUSH_AND_PULL, continuous = false)
        config.addCollection(col1a, colConfig1)
        config.addCollection(col1b, colConfig2)
        config.run()

        // Update "doc1" in colA and colB of database A.
        val mdoc1a = col1a.getDocument("doc1")!!.toMutable()
        mdoc1a.setString("update", "update1a")
        col1a.save(mdoc1a)

        val mdoc2a = col2a.getDocument("doc1")!!.toMutable()
        mdoc2a.setString("update", "update2a")
        col2a.save(mdoc2a)

        // Update "doc1" in colA and colB of database B.
        val mdoc1b = col1b.getDocument("doc2")!!.toMutable()
        mdoc1b.setString("update", "update1b")
        col1b.save(mdoc1b)

        val mdoc2b = col2b.getDocument("doc2")!!.toMutable()
        mdoc2b.setString("update", "update2b")
        col2b.save(mdoc2b)

        val r = Replicator(config)
        r.addDocumentReplicationListener { docReplication ->
            if (!docReplication.isPush) {
                // Pull will resolve the conflicts:
                val doc = docReplication.documents[0]
                assertEquals(if (doc.collection == "colA") "doc1" else "doc2", doc.id)
                assertNull(doc.error)

            } else {
                // Push will have conflict errors:
                for (doc in docReplication.documents) {
                    assertEquals(if (doc.collection == "colA") "doc1" else "doc2", doc.id)
                    assertEquals(CBLError.Code.HTTP_CONFLICT, doc.error?.code)
                }
            }
        }
        r.run()

        // verify the results
        var docA = col1a.getDocument("doc1")
        assertEquals("update1a", docA!!.getString("update"))
        docA = col2a.getDocument("doc1")
        assertEquals("update2a", docA!!.getString("update"))

        var docB = col1b.getDocument("doc2")
        assertEquals("update2b", docB!!.getString("update"))
        docB = col2b.getDocument("doc2")
        assertEquals("update2b", docB!!.getString("update"))
    }

    @Test
    fun testCollectionPushFilter() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        // Create some documents in colA and colB of the database A.
        createDocNumbered(col1a, 0, 10)
        createDocNumbered(col1b, 10, 10)

        val filter: ReplicationFilter = { doc, _ ->
            if (doc.collection!!.name == "colA") {
                doc.getInt("number1") < 5
            } else {
                doc.getInt("number1") >= 15
            }
        }

        val config1 = CollectionConfiguration()
        config1.pushFilter = filter

        val config2 = CollectionConfiguration()
        config2.pushFilter = filter

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeReplConfig(target = target, type = ReplicatorType.PUSH, continuous = false)
        config.addCollection(col1a, config1)
        config.addCollection(col1b, config2)
        config.run()

        assertEquals(10, col1a.count)
        assertEquals(10, col1b.count)
        assertEquals(5, col2a.count)
        assertEquals(5, col2b.count)
    }

    @Test
    fun testCollectionPullFilter() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        // Create some documents in colA and colB of the database A.
        createDocNumbered(col2a, 0, 10)
        createDocNumbered(col2b, 10, 10)

        val filter: ReplicationFilter = { doc, _ ->
            if (doc.collection!!.name == "colA") {
                doc.getInt("number1") < 5
            } else {
                doc.getInt("number1") >= 15
            }
        }

        val config1 = CollectionConfiguration()
        config1.pullFilter = filter

        val config2 = CollectionConfiguration()
        config2.pullFilter = filter

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeReplConfig(target = target, type = ReplicatorType.PULL, continuous = false)
        config.addCollection(col1a, config1)
        config.addCollection(col1b, config2)
        config.run()

        assertEquals(5, col1a.count)
        assertEquals(5, col1b.count)
        assertEquals(10, col2a.count)
        assertEquals(10, col2b.count)
    }

    @Test
    fun testCollectionDocumentIDsPushFilter() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col1a, 0, 5)
        createDocNumbered(col1b, 10, 5)

        val target = DatabaseEndpoint(targetDatabase)

        val config1 = CollectionConfiguration()
        config1.documentIDs = listOf("doc1", "doc2")

        val config2 = CollectionConfiguration()
        config2.documentIDs = listOf("doc10", "doc11", "doc13")

        val config = makeReplConfig(target = target, type = ReplicatorType.PUSH, continuous = false)
        config.addCollection(col1a, config1)
        config.addCollection(col1b, config2)
        config.run()

        assertEquals(5, col1a.count)
        assertEquals(5, col1b.count)
        assertEquals(2, col2a.count)
        assertEquals(3, col2b.count)
    }

    @Test
    fun testCollectionDocumentIDsPullFilter() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col2a, 0, 5)
        createDocNumbered(col2b, 10, 5)

        val target = DatabaseEndpoint(targetDatabase)

        val config1 = CollectionConfiguration()
        config1.documentIDs = listOf("doc1", "doc2")

        val config2 = CollectionConfiguration()
        config2.documentIDs = listOf("doc10", "doc11", "doc13")

        val config = makeReplConfig(target = target, type = ReplicatorType.PULL, continuous = false)
        config.addCollection(col1a, config1)
        config.addCollection(col1b, config2)
        config.run()

        assertEquals(2, col1a.count)
        assertEquals(3, col1b.count)
        assertEquals(5, col2a.count)
        assertEquals(5, col2b.count)
    }

    @Test
    fun testCollectionGetPendingDocumentIDs() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        targetDatabase.createCollection("colA", "scopeA")
        targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col1a, 0, 10)
        createDocNumbered(col1b, 10, 5)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a, col1b), target = target, type = ReplicatorType.PUSH, continuous = false)
        val r = Replicator(config)

        var docIds1a = r.getPendingDocumentIds(col1a)
        var docIds1b = r.getPendingDocumentIds(col1b)
        assertEquals(10, docIds1a.size)
        assertEquals(5, docIds1b.size)

        r.run()

        docIds1a = r.getPendingDocumentIds(col1a)
        docIds1b = r.getPendingDocumentIds(col1b)
        assertEquals(0, docIds1a.size)
        assertEquals(0, docIds1b.size)

        val mdoc1a = col1a.getDocument("doc1")!!.toMutable()
        mdoc1a.setString("update", "update1")
        col1a.save(mdoc1a)

        docIds1a = r.getPendingDocumentIds(col1a)
        assertEquals(1, docIds1a.size)
        assertTrue(docIds1a.contains("doc1"))

        val mdoc1b = col1b.getDocument("doc12")!!.toMutable()
        mdoc1b.setString("update", "update2")
        col1b.save(mdoc1b)

        docIds1b = r.getPendingDocumentIds(col1b)
        assertEquals(1, docIds1b.size)
        assertTrue(docIds1b.contains("doc12"))
    }

    @Test
    fun testCollectionIsDocumentPending() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        targetDatabase.createCollection("colA", "scopeA")
        targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col1a, 0, 10)
        createDocNumbered(col1b, 10, 5)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a, col1b), target = target, type = ReplicatorType.PUSH, continuous = false)
        val r = Replicator(config)

        // make sure all docs are pending
        for (i in 0..<10) {
            assertTrue(r.isDocumentPending("doc$i", col1a))
        }
        for (i in 10..<15) {
            assertTrue(r.isDocumentPending("doc$i", col1b))
        }

        r.run()

        // make sure, no document is pending
        for (i in 0..<10) {
            assertFalse(r.isDocumentPending("doc$i", col1a))
        }
        for (i in 10..<15) {
            assertFalse(r.isDocumentPending("doc$i", col1b))
        }

        // Update a document in colA.
        val mdoc1a = col1a.getDocument("doc1")!!.toMutable()
        mdoc1a.setString("update", "update1")
        col1a.save(mdoc1a)

        assertTrue(r.isDocumentPending("doc1", col1a))

        // Delete a document in colB.
        val mdoc1b = col1b.getDocument("doc12")!!.toMutable()
        col1b.delete(mdoc1b)

        assertTrue(r.isDocumentPending("doc12", col1b))
    }

    @Test
    fun testCollectionDocumentReplicationEvents() {
        val col1a = testDatabase.createCollection("colA", "scopeA")
        val col1b = testDatabase.createCollection("colB", "scopeA")

        val col2a = targetDatabase.createCollection("colA", "scopeA")
        val col2b = targetDatabase.createCollection("colB", "scopeA")

        createDocNumbered(col1a, 0, 4)
        createDocNumbered(col1b, 5, 5)

        val target = DatabaseEndpoint(targetDatabase)
        val config = makeSimpleReplConfig(source = listOf(col1a, col1b), target = target, type = ReplicatorType.PUSH_AND_PULL, continuous = false)
        val r = Replicator(config)
        var docCount = 0
        val docs = mutableListOf<String>()
        val token = r.addDocumentReplicationListener { docReplication ->
            docCount += docReplication.documents.size
            for (doc in docReplication.documents) {
                docs.add(doc.id)
            }
        }
        r.run()
        assertEquals(4, col1a.count)
        assertEquals(4, col2a.count)
        assertEquals(5, col1b.count)
        assertEquals(5, col2b.count)
        assertEquals(9, docCount)
        assertEquals(listOf("doc0", "doc1", "doc2", "doc3", "doc5", "doc6", "doc7", "doc8", "doc9"), docs.sorted())

        // colA & colB - db1
        val doc1 = col1a.getDocument("doc0")
        val doc2 = col1b.getDocument("doc6")
        assertNotNull(doc1)
        assertNotNull(doc2)
        col1a.delete(doc1)
        col1b.delete(doc2)

        // colA & colB - db2
        val doc3 = col2a.getDocument("doc1")
        val doc4 = col2b.getDocument("doc7")
        assertNotNull(doc3)
        assertNotNull(doc4)
        col2a.delete(doc3)
        col2b.delete(doc4)

        docCount = 0
        docs.clear()
        r.run()
        assertEquals(2, col1a.count)
        assertEquals(2, col2a.count)
        assertEquals(3, col1b.count)
        assertEquals(3, col2b.count)
        assertEquals(4, docCount)
        assertEquals(listOf("doc0", "doc1", "doc6", "doc7"), docs.sorted())

        token.remove()
    }
}
