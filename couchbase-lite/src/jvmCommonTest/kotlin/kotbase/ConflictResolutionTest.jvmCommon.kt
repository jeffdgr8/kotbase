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

import com.couchbase.lite.generation
import com.couchbase.lite.getC4Document
import kotlin.test.*

// TODO: in 3.2, these Java tests changed and only pass on Java/Android platforms
//  the previous 3.1 tests still pass on Objective-C and C platforms
//  see ConflictResolutionTest.native.kt
// These tests are largely translations of Jay Vavachan's Obj-C tests
class ConflictResolutionTest : BaseReplicatorTest() {

    /**
     * 1. Test conflict handler that just returns true without modifying the document.
     * 2. Test conflict handler that modifying the document and returns true.
     * 3. Make sure that the document is saved correctly for both scenarios.
     */
    @Test
    fun testConflictHandler() {
        val doc = createTestDoc()
        doc.setString("location", "Olympia")
        saveDocInCollection(doc)
        val docID = doc.id

        var ts1 = testCollection.getNonNullDoc(docID).generation

        val doc1a = testCollection.getNonNullDoc(docID).toMutable()
        val doc1b = testCollection.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        testCollection.save(doc1a)

        assertTrue(ts1 < testCollection.getNonNullDoc(docID).generation)
        var ts = testCollection.getNonNullDoc(docID).generation

        doc1b.setString("artist", "Holly Sears")
        var succeeded = testCollection.save(doc1b) { cur: MutableDocument, prev: Document? ->
            // the doc we are replacing is 1a and was saved more recently
            assertEquals(ts, (prev?.generation ?: 0))
            assertEquals(doc1a, prev)

            // the doc we are replacing it with is 1b and was saved earlier
            assertEquals(ts1, cur.generation)
            assertEquals(doc1b, cur)

            ts = cur.generation
            true
        }
        assertTrue(succeeded)

        val newDoc = testCollection.getNonNullDoc(docID)
        assertEquals(doc1b, newDoc)
        assertTrue(ts < newDoc.generation)
        ts1 = newDoc.generation

        val doc1c = testCollection.getNonNullDoc(docID).toMutable()
        val doc1d = testCollection.getNonNullDoc(docID).toMutable()

        doc1c.setString("artist", "Marjorie Morgan")
        testCollection.save(doc1c)

        assertTrue(ts < testCollection.getNonNullDoc(docID).generation)
        ts = testCollection.getNonNullDoc(docID).generation

        doc1d.setString("artist", "G. Charnelet-Vasselon")

        succeeded = testCollection.save(doc1d) { cur: MutableDocument, prev: Document? ->
            // the doc we are replacing is 1c and was saved more recently
            assertEquals(ts, (prev?.generation ?: 0))
            assertEquals(doc1c, prev)

            // the doc we are replacing it with is 1d and was saved earlier
            assertEquals(ts1, cur.generation)
            assertEquals(doc1d, cur)

            cur.setString("artist", "Sheep Jones")
            ts = cur.generation
            true
        }
        assertTrue(succeeded)

        val curDoc = testCollection.getNonNullDoc(docID)
        assertEquals("Olympia", curDoc.getString("location"))
        assertEquals("Sheep Jones", curDoc.getString("artist"))
        assertTrue(ts < curDoc.generation)
    }

    /**
     * 1. Test conflict handler that return false.
     * 2. Make sure that the save method return false as well and the original document has no change.
     */
    @Test
    fun testCancelConflictHandler() {
        val doc = createTestDoc()
        doc.setString("location", "Olympia")
        saveDocInCollection(doc)
        val docID = doc.id

        var ts = testCollection.getNonNullDoc(docID).generation

        val doc1a = testCollection.getNonNullDoc(docID).toMutable()
        val doc1b = testCollection.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        testCollection.save(doc1a)

        assertTrue(ts < testCollection.getNonNullDoc(docID).generation)
        ts = testCollection.getNonNullDoc(docID).generation

        doc1b.setString("artist", "Holly Sears")

        var tested = false
        var cDoc: MutableDocument? = null
        var pDoc: Document? = null
        var succeeded = testCollection.save(doc1b) { cur: MutableDocument, prev: Document? ->
            tested = true
            cDoc = cur
            pDoc = prev
            false
        }
        assertTrue(tested)
        assertEquals(doc1b, cDoc)
        assertEquals(doc1a, pDoc)
        assertFalse(succeeded)

        val curDoc = testCollection.getNonNullDoc(docID)
        assertEquals(curDoc, doc1a)

        // make sure no update to revision and generation
        assertEquals(doc1a.revisionID, curDoc.revisionID)
        assertEquals(ts, curDoc.generation)

        val doc1c = testCollection.getNonNullDoc(docID).toMutable()
        val doc1d = testCollection.getNonNullDoc(docID).toMutable()

        doc1c.setString("artist", "Marjorie Morgan")
        testCollection.save(doc1c)

        assertTrue(ts < testCollection.getNonNullDoc(docID).generation)
        ts = testCollection.getNonNullDoc(docID).generation

        doc1d.setString("artist", "G. Charnelet-Vasselon")

        tested = false
        succeeded = testCollection.save(doc1d) { cur, _ ->
            tested = true
            cur.setString("artist", "Holly Sears")
            false
        }
        assertTrue(tested)
        assertFalse(succeeded)

        // make sure no update to revision and generation
        val newDoc = testCollection.getNonNullDoc(docID)
        assertEquals(newDoc, doc1c)
        assertEquals(doc1c.revisionID, newDoc.revisionID)
        assertEquals(ts, newDoc.generation)
    }

    /**
     * 1. Test conflict handler that has an old doc as a deleted doc.
     * 2. Make sure that the old doc is null.
     * 3. Make sure that if returning true, the doc is saved correctly.
     *    If returning false, the document should be deleted as no change.
     */
    @Test
    fun testConflictHandlerWithDeletedOldDoc1() {
        val doc = createTestDoc()
        saveDocInCollection(doc)
        val docID = doc.id

        val doc1a = testCollection.getNonNullDoc(docID)
        val doc1b = testCollection.getNonNullDoc(docID).toMutable()

        testCollection.delete(doc1a, ConcurrencyControl.LAST_WRITE_WINS)

        doc1b.setString("location", "Olympia")

        val succeeded = testCollection.save(doc1b) { cur: MutableDocument, prev: Document? ->
            assertNotNull(cur)
            assertNull(prev)
            true
        }
        assertTrue(succeeded)

        assertEquals(doc1b, testCollection.getNonNullDoc(docID))
    }

    /**
     * 1. Test conflict handler that has an old doc as a deleted doc.
     * 2. Make sure that the old doc is null.
     * 3. Make sure that if returning true, the doc is saved correctly.
     *    If returning false, the document should be deleted as no change.
     */
    @Test
    fun testConflictHandlerWithDeletedOldDoc2() {
        val doc = createTestDoc()
        saveDocInCollection(doc)
        val docID = doc.id

        val doc1a = testCollection.getNonNullDoc(docID).toMutable()
        val doc1b = testCollection.getNonNullDoc(docID).toMutable()

        testCollection.delete(doc1a, ConcurrencyControl.LAST_WRITE_WINS)

        doc1b.setString("location", "Olympia")

        var tested = false
        var cDoc: MutableDocument? = null
        var pDoc: Document? = null
        val succeeded = testCollection.save(doc1b) { cur: MutableDocument, prev: Document? ->
            tested = true
            pDoc = prev
            cDoc = cur
            false
        }
        assertTrue(tested)
        assertNull(pDoc)
        assertNotNull(cDoc)
        assertFalse(succeeded)

        assertNull(testCollection.getDocument(docID))

        val c4doc = testCollection.getC4Document(docID)
        assertNotNull(c4doc)
        assertTrue(c4doc.isRevDeleted)
    }

    /**
     * 1. Test that an exception thrown from the conflict handler is captured and rethrown to the save method correctly.
     */
    @Test
    fun testConflictHandlerThrowsException() {
        val doc = createTestDoc()
        doc.setString("location", "Olympia")
        saveDocInCollection(doc)
        val docID = doc.id

        var ts = testCollection.getNonNullDoc(docID).generation

        val doc1a = testCollection.getNonNullDoc(docID).toMutable()
        val doc1b = testCollection.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        testCollection.save(doc1a)

        assertTrue(ts < testCollection.getNonNullDoc(docID).generation)
        ts = testCollection.getNonNullDoc(docID).generation

        doc1b.setString("artist", "Holly Sears")

        var tested = false
        var succeeded = false
        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.CONFLICT) {
            succeeded = testCollection.save(doc1b) { _: MutableDocument, _: Document? ->
                tested = true
                throw CouchbaseLiteError("freak out!")
            }
        }
        assertTrue(tested)
        assertFalse(succeeded)

        assertEquals(doc1a, testCollection.getNonNullDoc(docID))
        assertEquals(ts, testCollection.getNonNullDoc(docID).generation)
    }

    /**
     * 1. Test conflict handler that just returns true with modifying the document.
     *    It's possible that the conflict might happen again after trying to save the resolved document to the database.
     * 2. We could simulate this situation by update the local document before returning a resolved doc
     *    and make sure that the conflict resolver is called again.
     * 3. Make sure that the document is saved correctly with updated information, called twice.
     */
    @Test
    fun testCancelConflictHandlerCalledTwice() {
        val doc = createTestDoc()
        doc.setString("location", "Olympia")
        saveDocInCollection(doc)
        val docID = doc.id

        val ts1 = testCollection.getNonNullDoc(docID).generation

        val doc1a = testCollection.getNonNullDoc(docID).toMutable()
        val doc1b = testCollection.getNonNullDoc(docID).toMutable()

        doc1a.setString("artist", "Sheep Jones")
        testCollection.save(doc1a)
        assertTrue(ts1 < testCollection.getNonNullDoc(docID).generation)
        var ts = testCollection.getNonNullDoc(docID).generation

        doc1b.setString("artist", "Holly Sears")

        var count = 0
        val succeeded = testCollection.save(doc1b) { cur: MutableDocument, prev: Document? ->
            count++
            val doc1c = testCollection.getNonNullDoc(docID).toMutable()
            if (!doc1c.getBoolean("second update")) {
                assertEquals(ts1, cur.generation)
                assertEquals(ts, prev?.generation)
                ts = cur.generation

                doc1c.setBoolean("second update", true)
                val nDoc = saveDocInCollection(doc1c)

                assertTrue(ts < nDoc.generation)
                ts = nDoc.generation
            }

            val data = prev?.toMap()?.toMutableMap() ?: mutableMapOf()
            for (key in cur.keys) {
                data[key] = cur.getValue(key)
            }
            cur.setData(data)
            cur.setString("edit", "local")
            true
        }
        assertTrue(succeeded)

        assertEquals(2, count)

        val newDoc = testCollection.getNonNullDoc(docID)
        assertTrue(ts < newDoc.generation)
        assertEquals(newDoc.getString("location"), "Olympia")
        assertEquals(newDoc.getString("artist"), "Holly Sears")
        assertEquals(newDoc.getString("edit"), "local")
    }

    /**
     * 1. Get and make some changes to doc1a.
     * 2. Purge doc1b.
     * 3. Save doc1a, which should return false with error NotFound.
     */
    @Test
    fun testConflictHandlerWhenDocumentIsPurged() {
        val doc = createTestDoc()
        doc.setString("location", "Olympia")
        saveDocInCollection(doc)
        val docID = doc.id

        val doc1a = testCollection.getNonNullDoc(docID).toMutable()

        testCollection.purge(docID)

        doc1a.setString("artist", "Sheep Jones")

        assertThrowsCBLException(CBLError.Domain.CBLITE, CBLError.Code.NOT_FOUND) {
            testCollection.save(doc1a) { _: MutableDocument, _: Document? -> true }
        }
    }

//    /**
//     * CBL-1048: Logic bug in Conflict Resolution criteria
//     *
//     * Verify that the only docs subject to conflict resolution are conflicted docs that are being pulled
//     */
//    @Test
//    fun testConflictResolutionCriteria() = runBlocking {
//        // documentsEnded is going to need this.
//        val rc = ReplicationCollection.create(testCollection, null, null, null, null)
//
//        // An instrumented replicator: it counts documentEnded calls.
//        val conflictedCount = atomic(0)
//        val unconflictedCount = atomic(0)
//        val repl: AbstractReplicator = object :
//            AbstractReplicator(ReplicatorConfiguration(mockURLEndpoint).addCollection(testCollection, null)) {
//            override fun createReplicatorForTarget(target: Endpoint): C4Replicator = TODO("Not implemented")
//            override fun handleOffline(state: ReplicatorActivityLevel, online: Boolean) = TODO("Not implemented")
//
//            // Called to enqueue a conflict resolution: count conflicted docs
//            override fun runTaskConcurrently(task: Runnable) {
//                conflictedCount.getAndIncrement()
//            }
//
//            // Called when conflict resolution is not necessary: count unconflicted documents
//            override fun notifyDocumentEnded(pushing: Boolean, docs: List<ReplicatedDocument>) {
//                unconflictedCount.getAndAdd(docs.size)
//            }
//        }
//
//        val ends = listOf(
//            // A doc with no error
//            C4DocumentEnded(rc.token, rc.name, rc.scope, "foo-1", "22", 0, 2L, 0, 0, 0, true),
//            // A doc with error that is not a conflict
//            C4DocumentEnded(rc.token, rc.name, rc.scope, "foo-2", "22", 0, 2L, 1, 7, 0, true),
//            // This is the only conflicted doc
//            C4DocumentEnded(rc.token, rc.name, rc.scope, "foo-3", "22", 0, 2L, 1, 8, 0, true)
//        )
//
//        // This call to dispatch (pushing) should never enqueue a conflict resolution
//        repl.dispatchDocumentsEnded(ends, true)
//        // This call to dispatch should cause conflicted docs to be queued for resolution
//        repl.dispatchDocumentsEnded(ends, false)
//
//        // The calls to dispatchDocumentsEnded enqueue tasks on a serial queue.
//        // This task is the last thing on the queue: all the previously enqueued
//        // tasks are done when this is done
//        val latch = CountDownLatch(1)
//        InternalReplicatorTest.enqueueOnDispatcher(repl) { latch.countDown() }
//        assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
//
//        // A total of 6 docs dispatched; only one of them should have been enqued for CR
//        assertEquals(5, unconflictedCount.value)
//        assertEquals(1, conflictedCount.value)
//    }
}
