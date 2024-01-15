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

import com.couchbase.lite.copy
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
class NotificationTest : BaseDbTest() {

    @Test
    fun testCollectionChange() = runBlocking {
        val latch = CountDownLatch(1)

        val n = atomic(0)
        testCollection.addChangeListener(testSerialCoroutineContext) { change ->
            assertNotNull(change)
            assertEquals(testCollection, change.collection)
            val ids: List<String> = change.documentIDs
            assertNotNull(ids)
            if (n.addAndGet(ids.size) >= 10) { latch.countDown() }
        }.use {
            for (i in 0..<10) {
                val doc = MutableDocument()
                doc.setValue("type", "demo")
                saveDocInCollection(doc)
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        }
    }

    @Test
    fun testCollectionChangeOnSave() = runBlocking {
        val mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)

        // save doc
        val latch = CountDownLatch(1)
        testCollection.addDocumentChangeListener(mDocA.id) { change ->
            assertNotNull(change)
            assertEquals("A", change.documentID)
            assertEquals(1, latch.getCount())
            latch.countDown()
        }.use {
            saveDocInCollection(mDocB)
            saveDocInCollection(mDocA)
            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        }
    }

    @Test
    fun testCollectionChangeOnUpdate() = runBlocking {
        var mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInCollection(mDocA)
        var mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInCollection(mDocB)

        // update doc
        val latch = CountDownLatch(1)
        testCollection.addDocumentChangeListener(
            docA.id
        ) { change ->
            assertNotNull(change)
            assertEquals("A", change.documentID)
            assertEquals(1, latch.getCount())
            latch.countDown()
        }.use {
            mDocB = docB.toMutable()
            mDocB.setValue("thewronganswer", 42)
            saveDocInCollection(mDocB)

            mDocA = docA.toMutable()
            mDocA.setValue("thewronganswer", 18)
            saveDocInCollection(mDocA)
            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        }
    }

    @Test
    fun testCollectionChangeOnDelete() = runBlocking {
        val mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInCollection(mDocA)
        val mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInCollection(mDocB)

        // delete doc
        val latch = CountDownLatch(1)
        testCollection.addDocumentChangeListener(
            docA.id
        ) { change ->
            assertNotNull(change)
            assertEquals("A", change.documentID)
            assertEquals(1, latch.getCount())
            latch.countDown()
        }.use {
            testCollection.delete(docB)
            testCollection.delete(docA)
            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        }
    }

    @Test
    fun testExternalChanges() = runBlocking {
        val db2 = testDatabase.copy()
        val coll2 = db2.getSimilarCollection(testCollection)
        assertNotNull(coll2)

        val counter = atomic(0)

        var token: ListenerToken? = null
        try {
            val latchDB = CountDownLatch(1)
            coll2.addChangeListener(testSerialCoroutineContext) { change ->
                assertNotNull(change)
                if (counter.addAndGet(change.documentIDs.size) >= 10) {
                    assertEquals(1, latchDB.getCount())
                    latchDB.countDown()
                }
            }

            val latchDoc = CountDownLatch(1)
            token = coll2.addDocumentChangeListener("doc-6", testSerialCoroutineContext) { change ->
                assertNotNull(change)
                assertEquals("doc-6", change.documentID)
                val doc = coll2.getDocument(change.documentID)!!
                assertEquals("demo", doc.getString("type"))
                assertEquals(1, latchDoc.getCount())
                latchDoc.countDown()
            }

            testDatabase.inBatch {
                for (i in 0..<10) {
                    val doc = MutableDocument("doc-$i")
                    doc.setValue("type", "demo")
                    saveDocInCollection(doc)
                }
            }

            assertTrue(latchDB.await(STD_TIMEOUT_SEC.seconds))
            assertTrue(latchDoc.await(STD_TIMEOUT_SEC.seconds))
        } finally {
            token?.remove()
            db2.close()
        }
    }

    @Test
    fun testAddSameChangeListeners() = runBlocking {
        val doc1 = MutableDocument()
        val id = doc1.id
        doc1.setValue("name", "Scott")
        saveDocInCollection(doc1)

        val latch = CountDownLatch(5)
        // Add change listeners:
        val listener = { change: DocumentChange ->
            if (change.documentID == id) { latch.countDown() }
        }
        val token1 = testCollection.addDocumentChangeListener(id, listener)
        val token2 = testCollection.addDocumentChangeListener(id, listener)
        val token3 = testCollection.addDocumentChangeListener(id, listener)
        val token4 = testCollection.addDocumentChangeListener(id, listener)
        val token5 = testCollection.addDocumentChangeListener(id, listener)
        try {
            doc1.setValue("name", "Scott Tiger")
            saveDocInCollection(doc1)
            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        } finally {
            token1.remove()
            token2.remove()
            token3.remove()
            token4.remove()
            token5.remove()
        }
    }

    @Test
    fun testRemoveDocumentChangeListener() = runBlocking {
        var doc1 = MutableDocument()
        val id = doc1.id
        doc1.setValue("name", "Scott")
        var savedDoc1 = saveDocInCollection(doc1)

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(2)
        // Add change listeners:
        val listener = { change: DocumentChange ->
            if (change.documentID == id) {
                latch1.countDown()
                latch2.countDown()
            }
        }

        testCollection.addDocumentChangeListener(id, listener).use { token ->
            // Update doc1:
            doc1 = savedDoc1.toMutable()
            doc1.setValue("name", "Scott Tiger")
            savedDoc1 = saveDocInCollection(doc1)

            assertTrue(latch1.await(STD_TIMEOUT_SEC.seconds))

            // Remove change listener:
            token.remove()

            // Update doc1:
            doc1 = savedDoc1.toMutable()
            doc1.setValue("name", "Scotty")
            saveDocInCollection(doc1)

            assertFalse(latch2.await(500.milliseconds))
            assertEquals(1, latch2.getCount())
        }
    }

    // Internal API tests

//    @Test
//    fun testCollectionChangeNotifier() {
//        val changeNotifier = CollectionChangeNotifier(testCollection)
//        assertEquals(0, changeNotifier.getListenerCount())
//
//        val onRemove = { token: ListenerToken ->
//            val count: Int = changeNotifier.getListenerCount()
//            val empty: Boolean = changeNotifier.removeChangeListener(token)
//            assertTrue((count > 1) != empty)
//        }
//
//        val t1: ListenerToken = changeNotifier.addChangeListener(null, { c -> }, onRemove)
//        assertEquals(1, changeNotifier.getListenerCount())
//
//        val t2: ListenerToken = changeNotifier.addChangeListener(null, { c -> }, onRemove)
//        assertEquals(2, changeNotifier.getListenerCount())
//
//        t2.remove()
//        assertEquals(1, changeNotifier.getListenerCount())
//
//        t1.remove()
//        assertEquals(0, changeNotifier.getListenerCount())
//
//        t1.remove()
//        assertEquals(0, changeNotifier.getListenerCount())
//
//        t2.remove()
//        assertEquals(0, changeNotifier.getListenerCount())
//    }

    // CBL-4989 and CBL-4991: Check a few DocumentChange corner cases:
    // - null is a legal rev id
    // - null is not a legal doc id
    // - a list of changes that contains only nulls does not prevent further processing
    // - an empty change list does stop further processing
//    @Test
//    fun testCollectionChanged() = runBlocking {
//        val latch = CountDownLatch(1)
//        val changeCount = atomic(0)
//        val callCount = atomic(0)
//
//        val mockProducer: ChangeNotifier.C4ChangeProducer<C4DocumentChange> = object : C4ChangeProducer<C4DocumentChange?>() {
//            fun getChanges(maxChanges: Int): List<C4DocumentChange>? {
//                val n: Int = callCount.incrementAndGet()
//                return when (n) {
//                    1 -> listOf(C4DocumentChange.createC4DocumentChange("A", "r1", 0L, true))
//                    2 -> listOf(C4DocumentChange.createC4DocumentChange("B", null, 0L, true))
//                    3 -> listOf(C4DocumentChange.createC4DocumentChange(null, null, 0L, true))
//                    4 -> listOf<C4DocumentChange>()
//                    5 -> listOf(C4DocumentChange.createC4DocumentChange("C", "r1", 0L, true))
//                    6 -> null
//                    else -> listOf(C4DocumentChange.createC4DocumentChange("D", "r1", 0L, true))
//                }
//            }
//
//            fun close() {}
//        }
//
//        val notifier: CollectionChangeNotifier = CollectionChangeNotifier(testCollection)
//        notifier.addChangeListener(
//            null,
//            { ch ->
//                changeCount.addAndGet(ch.getDocumentIDs().size())
//                latch.countDown()
//            },
//            { ign -> })
//        notifier.run(mockProducer)
//
//        assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
//        assertEquals(3, changeCount.value)
//        assertEquals(6, callCount.value)
//    }

//    @Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")
//    @Test
//    fun testLegacyChangeAPI() = runBlocking {
//        val defaultCollection = testDatabase.defaultCollection
//
//        val latch1 = CountDownLatch(1)
//        val dbListener: DatabaseChangeListener = { latch1.countDown() }
//        dbListener(DatabaseChange(testDatabase, emptyList<Any>()))
//        assertTrue(latch1.await(STD_TIMEOUT_SEC.seconds))
//
//        val latch2 = CountDownLatch(1)
//        val colListener: CollectionChangeListener = { latch2.countDown() }
//        colListener(CollectionChange(testCollection, emptyList<Any>()))
//        assertTrue(latch2.await(STD_TIMEOUT_SEC.seconds))
//
//        val latch3 = CountDownLatch(2)
//        testDatabase.addChangeListener { latch3.countDown() }.use { ignore1 ->
//            defaultCollection!!.addChangeListener { latch3.countDown() }.use {
//                assertEquals(2, defaultCollection.getCollectionListenerCount())
//                saveDocsInCollection(createTestDocs(1000, 10), defaultCollection)
//                assertTrue(latch3.await(STD_TIMEOUT_SEC.seconds))
//            }
//        }
//        assertEquals(0, defaultCollection.getCollectionListenerCount())
//
//        val latch4 = CountDownLatch(2)
//        testDatabase.addChangeListener { latch4.countDown() }.use {
//            defaultCollection!!.addChangeListener { latch4.countDown() }.use {
//                assertEquals(2, defaultCollection.getCollectionListenerCount())
//                saveDocsInCollection(createTestDocs(2000, 10), defaultCollection)
//                assertTrue(latch4.await(STD_TIMEOUT_SEC.seconds))
//            }
//        }
//        assertEquals(0, defaultCollection.getCollectionListenerCount())
//    }
}
