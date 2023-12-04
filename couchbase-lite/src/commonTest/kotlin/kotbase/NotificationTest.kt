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
import kotbase.test.lockWithTimeout
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.coroutines.sync.Mutex
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// !!! ADD COLLECTION NOTIFICATION TESTS

class NotificationTest : BaseDbTest() {

    @Test
    fun testDatabaseChange() = runBlocking {
        val mutex = Mutex(true)

        val n = intArrayOf(0)

        @Suppress("UNUSED_VARIABLE")
        val token = baseTestDb.addChangeListener { change ->
            assertNotNull(change)
            assertEquals(baseTestDb, change.database)
            val ids: List<String> = change.documentIDs
            assertNotNull(ids)
            n[0] += ids.size
            if (n[0] >= 10) {
                mutex.unlock()
            }
        }

        for (i in 0..<10) {
            val doc = MutableDocument("doc-$i")
            doc.setValue("type", "demo")
            saveDocInBaseTestDb(doc)
        }

        assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
    }

    @Test
    fun testDocumentChangeOnSave() = runBlocking {
        val mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)

        // save doc
        val mutex = Mutex(true)
        val token = baseTestDb.addDocumentChangeListener(mDocA.id) { change ->
            assertNotNull(change)
            assertEquals("A", change.documentID)
            assertTrue(mutex.isLocked)
            mutex.unlock()
        }
        try {
            saveDocInBaseTestDb(mDocB)
            saveDocInBaseTestDb(mDocA)
            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        } finally {
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)
        }
    }

    @Test
    fun testDocumentChangeOnUpdate() = runBlocking {
        var mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInBaseTestDb(mDocA)
        var mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInBaseTestDb(mDocB)

        // update doc
        val mutex = Mutex(true)
        val token = baseTestDb.addDocumentChangeListener(docA.id) { change ->
            assertNotNull(change)
            assertEquals("A", change.documentID)
            assertTrue(mutex.isLocked)
            mutex.unlock()
        }
        try {
            mDocB = docB.toMutable()
            mDocB.setValue("thewronganswer", 42)
            saveDocInBaseTestDb(mDocB)

            mDocA = docA.toMutable()
            mDocA.setValue("thewronganswer", 18)
            saveDocInBaseTestDb(mDocA)
            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        } finally {
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)
        }
    }

    @Test
    fun testDocumentChangeOnDelete() = runBlocking {
        val mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInBaseTestDb(mDocA)
        val mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInBaseTestDb(mDocB)

        // delete doc
        val mutex = Mutex(true)
        val token = baseTestDb.addDocumentChangeListener(docA.id) { change ->
            assertNotNull(change)
            assertEquals("A", change.documentID)
            assertTrue(mutex.isLocked)
            mutex.unlock()
        }
        try {
            baseTestDb.delete(docB)
            baseTestDb.delete(docA)
            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        } finally {
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)
        }
    }

    @Test
    fun testExternalChanges() = runBlocking {
        val db2 = baseTestDb.copy()
        assertNotNull(db2)

        val counter = atomic(0)

        var token: ListenerToken? = null
        try {
            val mutexDB = Mutex(true)
            db2.addChangeListener { change ->
                assertNotNull(change)
                if (counter.addAndGet(change.documentIDs.size) >= 10) {
                    assertTrue(mutexDB.isLocked)
                    mutexDB.unlock()
                }
            }

            val mutexDoc = Mutex(true)
            token = db2.addDocumentChangeListener("doc-6") { change ->
                assertNotNull(change)
                assertEquals("doc-6", change.documentID)
                val doc = db2.getDocument(change.documentID)!!
                assertEquals("demo", doc.getString("type"))
                assertTrue(mutexDoc.isLocked)
                mutexDoc.unlock()
            }

            baseTestDb.inBatch {
                for (i in 0..<10) {
                    val doc = MutableDocument("doc-$i")
                    doc.setValue("type", "demo")
                    saveDocInBaseTestDb(doc)
                }
            }

            assertTrue(mutexDB.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
            assertTrue(mutexDoc.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        } finally {
            // TODO: 3.1 API
            //token?.remove()
            if (token != null) {
                db2.removeChangeListener(token)
            }
            db2.close()
        }
    }

    @Test
    fun testAddSameChangeListeners() = runBlocking {
        var doc1 = MutableDocument("doc1")
        doc1.setValue("name", "Scott")
        val savedDoc1 = saveDocInBaseTestDb(doc1)

        val latch = CountDownLatch(5)
        // Add change listeners:
        val listener = { change: DocumentChange ->
            if (change.documentID == "doc1") {
                latch.countDown()
            }
        }
        val token1 = baseTestDb.addDocumentChangeListener("doc1", listener)
        val token2 = baseTestDb.addDocumentChangeListener("doc1", listener)
        val token3 = baseTestDb.addDocumentChangeListener("doc1", listener)
        val token4 = baseTestDb.addDocumentChangeListener("doc1", listener)
        val token5 = baseTestDb.addDocumentChangeListener("doc1", listener)

        try {
            // Update doc1:
            doc1 = savedDoc1.toMutable()
            doc1.setValue("name", "Scott Tiger")
            saveDocInBaseTestDb(doc1)

            // Let's only wait for 0.5 seconds:
            assertTrue(latch.await(500.milliseconds))
        } finally {
            // TODO: 3.1 API
            //token1.remove()
            //token2.remove()
            //token3.remove()
            //token4.remove()
            //token5.remove()
            baseTestDb.removeChangeListener(token1)
            baseTestDb.removeChangeListener(token2)
            baseTestDb.removeChangeListener(token3)
            baseTestDb.removeChangeListener(token4)
            baseTestDb.removeChangeListener(token5)
        }
    }

    @Test
    fun testRemoveDocumentChangeListener() = runBlocking {
        var doc1 = MutableDocument("doc1")
        doc1.setValue("name", "Scott")
        var savedDoc1 = saveDocInBaseTestDb(doc1)

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(2)
        // Add change listeners:
        val listener = { change: DocumentChange ->
            if (change.documentID == "doc1") {
                latch1.countDown()
                latch2.countDown()
            }
        }

        val token = baseTestDb.addDocumentChangeListener("doc1", listener)
        try {
            // Update doc1:
            doc1 = savedDoc1.toMutable()
            doc1.setValue("name", "Scott Tiger")
            savedDoc1 = saveDocInBaseTestDb(doc1)

            // Let's only wait for 0.5 seconds:
            assertTrue(latch1.await(500.milliseconds))

            // Remove change listener:
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)

            // Update doc1:
            doc1 = savedDoc1.toMutable()
            doc1.setValue("name", "Scotty")
            saveDocInBaseTestDb(doc1)

            assertFalse(latch2.await(500.milliseconds))
            assertEquals(1, latch2.getCount())
        } finally {
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)
        }
    }

    // TODO: 3.1 API
//    @Test
//    fun testDatabaseChangeNotifier() {
//        val db = createDb("default_config_db")
//        val changeNotifier = CollectionChangeNotifier(db.getDefaultCollection())
//        assertEquals(0, changeNotifier.getListenerCount())
//        val t1: ListenerToken = changeNotifier.addChangeListener(
//            null,
//            { c -> }
//        ) { t -> assertTrue(changeNotifier.removeChangeListener(t)) }
//        assertEquals(1, changeNotifier.getListenerCount())
//        val t2: ListenerToken = changeNotifier.addChangeListener(
//            null,
//            { c -> }
//        ) { t -> assertFalse(changeNotifier.removeChangeListener(t)) }
//        assertEquals(2, changeNotifier.getListenerCount())
//        t2.remove()
//        assertEquals(1, changeNotifier.getListenerCount())
//        t1.remove()
//        assertEquals(0, changeNotifier.getListenerCount())
//        t1.remove()
//        assertEquals(0, changeNotifier.getListenerCount())
//        t2.remove()
//        assertEquals(0, changeNotifier.getListenerCount())
//    }

    // TODO: 3.1 API
//    @Test
//    fun testDatabaseChangeAPI() = runBlocking {
//        val mutex1 = Mutex(true)
//        val dbListener = { change: DatabaseChange ->
//            mutex1.unlock()
//        }
//        dbListener(DatabaseChange(baseTestDb.getDefaultCollection(), emptyList()))
//        assertTrue(mutex1.lockWithTimeout(STD_TIMEOUT_MS.milliseconds))
//        val mutex2 = Mutex(true)
//        val colListener = { change: CollectionChange ->
//            mutex2.unlock()
//        }
//        colListener(CollectionChange(baseTestDb.getDefaultCollection(), emptyList()))
//        assertTrue(mutex2.lockWithTimeout(STD_TIMEOUT_MS.milliseconds))
//
//        val latch3 = CountDownLatch(2)
//        var t1: ListenerToken? = null
//        var t2: ListenerToken? = null
//        try {
//            t1 = baseTestDb.addChangeListener { change: DatabaseChange) ->
//                latch3.countDown()
//            }
//            t2 = baseTestDb.getDefaultCollection().addChangeListener { change: CollectionChange) ->
//                latch3.countDown()
//            }
//            assertEquals(2, baseTestDb.getDefaultCollection().getCollectionListenerCount())
//            createDocsInDb(1000, 1, baseTestDb)
//            assertTrue(latch3.await(STD_TIMEOUT_MS.milliseconds))
//        } finally {
//            t1?.remove()
//            t2?.remove()
//        }
//        assertEquals(0, baseTestDb.getDefaultCollection().getCollectionListenerCount())
//
//        val latch4 = CountDownLatch(2)
//        var t3: ListenerToken? = null
//        var t4: ListenerToken? = null
//        try {
//            t3 = baseTestDb.addChangeListener { change -> latch4.countDown() }
//            t4 = baseTestDb.getDefaultCollection().addChangeListener { change ->
//                latch4.countDown()
//            }
//            assertEquals(2, baseTestDb.getDefaultCollection().getCollectionListenerCount())
//            createDocsInDb(2000, 1, baseTestDb)
//            assertTrue(latch4.await(STD_TIMEOUT_MS.milliseconds))
//        } finally {
//            t3?.remove()
//            t4?.remove()
//        }
//        assertEquals(0, baseTestDb.getDefaultCollection().getCollectionListenerCount())
//    }
}
