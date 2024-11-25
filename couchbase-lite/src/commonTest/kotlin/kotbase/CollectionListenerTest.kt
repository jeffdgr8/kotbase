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

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.datetime.Clock
import kotlin.coroutines.CoroutineContext
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
class CollectionListenerTest : BaseDbTest() {

    // 8.4.1 Test that change listeners can be added to a collection and that they receive changes correctly.
    @Test
    fun testCollectionChangeListener() = runBlocking {
        testCollectionChangeListener(null)
    }

    // 8.4.2 Test that change listeners can be added to a collection with a custom coroutine context
    //   and that they receive changes correctly.
    @Test
    fun testCollectionChangeListenerWithExecutor() = runBlocking {
        testCollectionChangeListener(testSerialCoroutineContext)
    }

    // 8.4.3 Test that document change listeners can be added to a collection and that they receive changes correctly.
    @Test
    fun testCollectionDocumentChangeListener() = runBlocking {
        testCollectionDocumentChangeListener(null)
    }

    // 8.4.4 Test that document change listeners can be added to a collection with a custom executor
    //   and that they receive changes correctly.
    @Test
    fun testCollectionDocumentChangeListenerWithExecutor() = runBlocking {
        testCollectionDocumentChangeListener(testSerialCoroutineContext)
    }

    // Test that adding a change listener to a deleted collection doesn't throw exception
    @Test
    fun testAddChangeListenerToDeletedCollection() {
        testDatabase.deleteCollection(testCollection.name, testCollection.scope.name)
        testCollection.addChangeListener(testSerialCoroutineContext) {}
    }


    // Test that addChangeListener to a collection deleted from a different db instance doesn't throw exception
    @Test
    fun testAddChangeListenerToCollectionDeletedInDifferentDBInstance() {
        duplicateDb(testDatabase).use {
            it.deleteCollection(testCollection.name, testCollection.scope.name)
            testCollection.addChangeListener(testSerialCoroutineContext) {}
        }
    }

    // Test that adding a document change listener to a deleted collection
    // doesn't throw exception
    @Test
    fun testAddDocumentChangeListenerToDeletedCollection() {
        testDatabase.deleteCollection(testCollection.name, testCollection.scope.name)
        testCollection.addDocumentChangeListener("doc_id", testSerialCoroutineContext) {}
    }

    // Test that adding a document change listener to a deleted collection gets warning message
    // and doesn't throw exception
    @Test
    fun testAddDocumentChangeListenerToCollectionDeletedInADifferentDBInstance() {
        duplicateDb(testDatabase).use {
            it.deleteCollection(testCollection.name, testCollection.scope.name)
            testCollection.addDocumentChangeListener("doc_id", testSerialCoroutineContext) {}
        }
    }

    // Test that removing a listener from a deleted collection doesn't throw exception
    @Test
    fun testRemoveChangeListenerFromDeletedCollection() {
        val token = testCollection.addChangeListener { }
        try {
            testDatabase.deleteCollection(testCollection.name, testCollection.scope.name)
            assertNull(testDatabase.getCollection(testCollection.name, testCollection.scope.name))
        } finally {
            token.remove()
        }
    }

    // Test that removing a listener from a collection deleted in a different db doesn't throw exception
    @Test
    fun testRemoveChangeListenerFromCollectionDeletedInADifferentDBInstance() {
        val otherDb = duplicateDb(testDatabase)
        val token = testCollection.addChangeListener { }

        try {
            otherDb.deleteCollection(testCollection.name, testCollection.scope.name)
            assertNull(testDatabase.getCollection(testCollection.name, testCollection.scope.name))
        } finally {
            token.remove()

            otherDb.close()
        }
    }

    // Test that removing a listener from a deleted collection doesn't throw exception
    @Test
    fun testRemoveDocChangeListenerFromDeletedCollection() {
        val docId = "doc_1"
        testCollection.save(MutableDocument(docId))

        val token = testCollection.addDocumentChangeListener(docId) { }
        try {
            testDatabase.deleteCollection(testCollection.name, testCollection.scope.name)
            assertNull(testDatabase.getCollection(testCollection.name, testCollection.scope.name))

        } finally {
            token.remove()
        }
    }

    // Test that removing a listener from a collection deleted in a different db doesn't throw exception
    @Test
    fun testRemoveDocChangeListenerFromCollectionDeletedInADifferentDBInstance() {
        val otherDb = duplicateDb(testDatabase)
        val token = testCollection.addChangeListener { }

        try {
            otherDb.deleteCollection(testCollection.name, testCollection.scope.name)
            assertNull(testDatabase.getCollection(testCollection.name, testCollection.scope.name))
        } finally {
            token.remove()

            otherDb.close()
        }
    }

    // Test that adding a change listener to a collection in a closed database doesn't throw an exception
    @Test
    fun testAddChangeListenerToCollectionInClosedDatabase() {
        testDatabase.close()
        testCollection.addChangeListener {}
    }

    // Test that adding a document change listener  to a collection in a closed database doesn't throw an exception
    @Test
    fun testAddDocumentChangeListenerToCollectionInClosedDatabase() {
        val docID = "testDoc"
        testCollection.save(MutableDocument(docID))

        testDatabase.close()

        testCollection.addDocumentChangeListener(docID) {}
    }

    // Test that removing a listener from a collection in a closed database doesn't throw exception
    @Test
    fun testRemoveChangeListenerFromCollectionInClosedDatabase() {
        val token = testCollection.addChangeListener {}
        try {
            testDatabase.close()
        } finally {
            token.remove()
        }
    }

    // Test that addChangeListener to a collection in a deleted database doesn't throw an exception
    @Test
    fun testAddChangeListenerToCollectionInDeletedDatabase() {
        eraseDb(testDatabase)
        testCollection.addChangeListener {}
    }

    // Test that addDocumentChangeListener to a collection in a deleted database doesn't throw an exception
    @Test
    fun testAddDocumentChangeListenerToCollectionInDeletedDatabase() {
        eraseDb(testDatabase)
        testCollection.addDocumentChangeListener("doc_id") {}
    }

    // Test that removeChangeListener from a collection in a deleted database doesn't throw exception
    @Test
    fun testRemoveChangeListenerFromCollectionInDeletedDatabase() {
        val token = testCollection.addChangeListener { }
        try {
            eraseDb(testDatabase)
        } finally {
            token.remove()
        }
    }

    // These tests are incredibly finicky.

    // Create two collections, A and B.
    // Add two change listeners to collection A.
    // Create documents, update documents, and delete Documents to/from both collections.
    // Ensure that the two listeners received only the changes from the collection A.
    // Remove the two change listeners by using token.remove() API.
    // Update the documents on the collection A.
    // Ensure that the two listeners don’t receive any changes.
    private suspend fun testCollectionChangeListener(coroutineContext: CoroutineContext?) {
        val doc1Id = "doc_1"
        val doc2Id = "doc_2"
        val doc3Id = "doc_3"

        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        val changes1 = mutableListOf<String>()
        val changes2 = mutableListOf<String>()

        var latch = CountDownLatch(2)

        var t = 0L

        val listener1 = { c: CollectionChange ->
            changes1.addAll(c.documentIDs)
            if (changes1.size >= 2) {
                latch.countDown()
            }
        }
        val token1 = if (coroutineContext != null) {
            collectionA.addChangeListener(coroutineContext, listener1)
        } else {
            collectionA.addChangeListener(listener1)
        }

        val listener2 = { c: CollectionChange ->
            changes2.addAll(c.documentIDs)
            if (changes2.size >= 2) {
                latch.countDown()
            }
        }
        val token2 = if (coroutineContext != null) {
            collectionA.addChangeListener(coroutineContext, listener2)
        } else {
            collectionA.addChangeListener(listener2)
        }

        t -= Clock.System.now().toEpochMilliseconds()
        collectionB.save(MutableDocument(doc3Id))
        collectionA.save(MutableDocument(doc2Id))
        collectionA.save(MutableDocument(doc1Id))

        assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        t += Clock.System.now().toEpochMilliseconds()

        assertEquals(2, changes1.size)
        assertTrue(changes1.contains(doc1Id))
        assertTrue(changes1.contains(doc2Id))
        assertEquals(2, changes2.size)
        assertTrue(changes1.contains(doc1Id))
        assertTrue(changes1.contains(doc2Id))

        // Update documents
        latch = CountDownLatch(2)
        changes1.clear()
        changes2.clear()

        t -= Clock.System.now().toEpochMilliseconds()

        collectionB.save(collectionB.getDocument(doc3Id)!!.toMutable().setString("Lucky", "Radiohead"))
        collectionA.save(collectionA.getDocument(doc2Id)!!.toMutable().setString("Dazzle", "Siouxsie & the Banshees"))
        collectionA.save(
            collectionA.getDocument(doc1Id)!!.toMutable().setString("Baroud", "Cheb Khaled & Safy Boutella")
        )

        assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        t += Clock.System.now().toEpochMilliseconds()

        assertEquals(2, changes1.size)
        assertTrue(changes1.contains(doc1Id))
        assertTrue(changes1.contains(doc2Id))
        assertEquals(2, changes2.size)
        assertTrue(changes1.contains(doc1Id))
        assertTrue(changes1.contains(doc2Id))

        // Delete documents
        latch = CountDownLatch(2)
        changes1.clear()
        changes2.clear()

        t -= Clock.System.now().toEpochMilliseconds()
        collectionB.delete(collectionB.getDocument(doc3Id)!!)
        collectionA.delete(collectionA.getDocument(doc1Id)!!)
        collectionA.delete(collectionA.getDocument(doc2Id)!!)

        assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        t += Clock.System.now().toEpochMilliseconds()

        assertEquals(2, changes1.size)
        assertTrue(changes1.contains(doc1Id))
        assertTrue(changes1.contains(doc2Id))
        assertEquals(2, changes2.size)
        assertTrue(changes1.contains(doc1Id))
        assertTrue(changes1.contains(doc2Id))

        latch = CountDownLatch(2)
        changes1.clear()
        changes2.clear()

        // Remove the change listeners
        token1.remove()
        token2.remove()

        collectionB.save(MutableDocument(doc3Id))
        collectionA.save(MutableDocument(doc2Id))
        collectionA.save(MutableDocument(doc1Id))

        // wait twice the average time to notify
        assertFalse(latch.await(((t * 2) / 3).milliseconds))
        assertTrue(changes1.isEmpty())
        assertTrue(changes2.isEmpty())
    }

    // Create two collections, A and B.
    // Add two document change listeners to collection A.
    // Create documents, update documents, and delete Documents to/from both collections.
    // Ensure that the two listeners received only the changes from the collection A.
    // Remove the two change listeners by using token.remove() API.
    // Update the documents on the collection A.
    // Ensure that the two listeners don’t receive any changes.
    private suspend fun testCollectionDocumentChangeListener(coroutineContext: CoroutineContext?) {
        val collectionA = testDatabase.createCollection("colA", "scopeA")
        val collectionB = testDatabase.createCollection("colB", "scopeA")

        var latch: CountDownLatch? = null
        var changes1: MutableList<String>? = null
        var changes2: MutableList<String>? = null

        val doc1Id = "doc_1"
        val doc2Id = "doc_2"
        val doc3Id = "doc_3"

        var t = 0L

        val listener1: (DocumentChange) -> Unit = { c ->
            changes1?.add(c.documentID)
            latch?.countDown()
        }
        val token1 = if (coroutineContext != null) {
            collectionA.addDocumentChangeListener(doc1Id, coroutineContext, listener1)
        } else {
            collectionA.addDocumentChangeListener(doc1Id, listener1)
        }

        val listener2: (DocumentChange) -> Unit = { c ->
            changes2?.add(c.documentID)
            latch?.countDown()
        }
        val token2 = if (coroutineContext != null) {
            collectionA.addDocumentChangeListener(doc2Id, coroutineContext, listener2)
        } else {
            collectionA.addDocumentChangeListener(doc2Id, listener2)
        }

        try {
            // Create documents
            latch = CountDownLatch(2)
            changes1 = mutableListOf()
            changes2 = mutableListOf()

            t -= Clock.System.now().toEpochMilliseconds()
            collectionB.save(MutableDocument(doc3Id))
            collectionA.save(MutableDocument(doc2Id))
            collectionA.save(MutableDocument(doc1Id))

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            t += Clock.System.now().toEpochMilliseconds()

            assertEquals(1, changes1.size)
            assertTrue(changes1.contains(doc1Id))
            assertEquals(1, changes2.size)
            assertTrue(changes2.contains(doc2Id))

            // Update documents
            latch = CountDownLatch(2)
            changes1.clear()
            changes2.clear()

            t -= Clock.System.now().toEpochMilliseconds()
            collectionB.save(
                collectionB.getDocument(doc3Id)?.toMutable()?.setString("Lucky", "Radiohead")!!
            )
            collectionA.save(
                collectionA.getDocument(doc2Id)?.toMutable()?.setString("Dazzle", "Siouxsie & the Banshees")!!
            )
            collectionA.save(
                collectionA.getDocument(doc1Id)?.toMutable()?.setString("Baroud", "Cheb Khaled & Safy Boutella")!!
            )

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            t += Clock.System.now().toEpochMilliseconds()

            assertEquals(1, changes1.size)
            assertTrue(changes1.contains(doc1Id))
            assertEquals(1, changes2.size)
            assertTrue(changes2.contains(doc2Id))

            // Delete documents
            latch = CountDownLatch(2)
            changes1.clear()
            changes2.clear()

            t -= Clock.System.now().toEpochMilliseconds()
            collectionB.delete(collectionB.getDocument(doc3Id)!!)
            collectionA.delete(collectionA.getDocument(doc2Id)!!)
            collectionA.delete(collectionA.getDocument(doc1Id)!!)

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            t += Clock.System.now().toEpochMilliseconds()

            assertEquals(1, changes1.size)
            assertTrue(changes1.contains(doc1Id))
            assertEquals(1, changes2.size)
            assertTrue(changes2.contains(doc2Id))

            // Remove the change listeners
            latch = CountDownLatch(2)
            changes1.clear()
            changes2.clear()

        } finally {
            token1.remove()
            token2.remove()
        }

        collectionB.save(MutableDocument(doc3Id))
        collectionA.save(MutableDocument(doc2Id))
        collectionA.save(MutableDocument(doc1Id))

        // wait twice the average time to notify
        assertFalse(latch.await(((t * 2) / 3).milliseconds))
        assertTrue(changes1.isEmpty())
        assertTrue(changes2.isEmpty())
    }
}
