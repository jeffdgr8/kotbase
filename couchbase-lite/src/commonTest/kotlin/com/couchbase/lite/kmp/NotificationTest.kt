package com.couchbase.lite.kmp

import com.couchbase.lite.copy
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.assertFailsWith
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// !!! ADD COLLECTION NOTIFICATION TESTS

class NotificationTest : BaseDbTest() {

    @Test
    fun testDatabaseChange() = runBlocking {
        val mutex = Mutex(true)

        val n = intArrayOf(0)
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

        for (i in 0 until 10) {
            val doc = MutableDocument("doc-$i")
            doc.setValue("type", "demo")
            saveDocInBaseTestDb(doc)
        }

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }
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
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
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
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
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
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
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
                for (i in 0 until 10) {
                    val doc = MutableDocument("doc-$i")
                    doc.setValue("type", "demo")
                    saveDocInBaseTestDb(doc)
                }
            }

            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutexDB.lock()
            }
            withTimeout(STD_TIMEOUT_SEC.seconds) {
                mutexDoc.lock()
            }
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
            withTimeout(500.milliseconds) {
                latch.await()
            }
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
            withTimeout(500.milliseconds) {
                latch1.await()
            }

            // Remove change listener:
            // TODO: 3.1 API
            //token.remove()
            baseTestDb.removeChangeListener(token)

            // Update doc1:
            doc1 = savedDoc1.toMutable()
            doc1.setValue("name", "Scotty")
            saveDocInBaseTestDb(doc1)

            assertFailsWith<TimeoutCancellationException> {
                withTimeout(500.milliseconds) {
                    latch2.await()
                }
            }
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
//        withTimeout(STD_TIMEOUT_MS.milliseconds) {
//            mutex1.lock()
//        }
//        val mutex2 = Mutex(true)
//        val colListener = { change: CollectionChange ->
//            mutex2.unlock()
//        }
//        colListener(CollectionChange(baseTestDb.getDefaultCollection(), emptyList()))
//        withTimeout(STD_TIMEOUT_MS.milliseconds) {
//            mutex2.lock()
//        }
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
//            withTimeout(STD_TIMEOUT_MS.milliseconds) {
//                latch3.await()
//            }
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
//            withTimeout(STD_TIMEOUT_MS.milliseconds) {
//                latch4.await()
//            }
//        } finally {
//            t3?.remove()
//            t4?.remove()
//        }
//        assertEquals(0, baseTestDb.getDefaultCollection().getCollectionListenerCount())
//    }
}
