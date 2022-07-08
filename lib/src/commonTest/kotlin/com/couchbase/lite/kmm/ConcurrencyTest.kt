//// TODO:
//package com.couchbase.lite.kmm
//
//import com.couchbase.lite.internal.utils.ConcurrencyUnitTest
//import com.couchbase.lite.kmm.internal.utils.Report
//import com.udobny.kmm.use
//import kotlinx.datetime.Clock
//import kotlin.Array
//import kotlin.test.*
//
//
//class ConcurrencyTest : BaseDbTest() {
//
//    internal interface Callback {
//        fun callback(threadIndex: Int)
//    }
//
//    internal interface VerifyBlock<T> {
//        fun verify(n: Int, result: T)
//    }
//
//    private val testFailure: java.util.concurrent.atomic.AtomicReference<java.lang.AssertionError> =
//        java.util.concurrent.atomic.AtomicReference<java.lang.AssertionError>()
//
//    @BeforeTest
//    fun setUpConcurrencyTest() {
//        testFailure.set(null)
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(CouchbaseLiteException::class)
//    fun testConcurrentCreate() {
//        Database.log.console.level = LogLevel.DEBUG
//        val kNDocs = 50
//        val kNThreads = 4
//        val kWaitInSec = 180
//
//        // concurrently creates documents
//        concurrentValidator(
//            kNThreads,
//            kWaitInSec,
//            Callback { threadIndex: Int ->
//                val tag = "tag-$threadIndex"
//                try {
//                    createDocs(kNDocs, tag)
//                } catch (e: CouchbaseLiteException) {
//                    fail()
//                }
//            }
//        )
//
//        // validate stored documents
//        for (i in 0 until kNThreads) {
//            verifyByTagName("tag-$i", kNDocs)
//        }
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(CouchbaseLiteException::class)
//    fun testConcurrentCreateInBatch() {
//        val kNDocs = 50
//        val kNThreads = 4
//        val kWaitInSec = 180
//
//        // concurrently creates documents
//        concurrentValidator(
//            kNThreads,
//            kWaitInSec,
//            Callback { threadIndex: Int ->
//                val tag = "tag-$threadIndex"
//                try {
//                    baseTestDb.inBatch { createDocs(kNDocs, tag) }
//                } catch (e: CouchbaseLiteException) {
//                    fail()
//                }
//            }
//        )
//        checkForFailure()
//
//        // validate stored documents
//        for (i in 0 until kNThreads) {
//            verifyByTagName("tag-$i", kNDocs)
//        }
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(CouchbaseLiteException::class)
//    fun testConcurrentUpdate() {
//        // ??? Increasing number of threads causes crashes
//        val nDocs = 5
//        val nThreads = 4
//
//        // createDocs2 returns synchronized List.
//        val docIDs = createDocs(nDocs, "Create")
//        assertEquals(nDocs, docIDs.size)
//
//        // concurrently creates documents
//        concurrentValidator(
//            nThreads,
//            600,
//            Callback { threadIndex: Int ->
//                val tag = "tag-$threadIndex"
//                assertTrue(updateDocs(docIDs, 50, tag))
//            }
//        )
//        val count: java.util.concurrent.atomic.AtomicInteger =
//            java.util.concurrent.atomic.AtomicInteger(0)
//        for (i in 0 until nThreads) {
//            verifyByTagName(
//                "tag-$i",
//                VerifyBlock<Result> { n: Int, result: Result? -> count.incrementAndGet() })
//        }
//        assertEquals(nDocs.toLong(), count.toInt().toLong())
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(CouchbaseLiteException::class)
//    fun testConcurrentRead() {
//        val kNDocs = 5
//        val kNRounds = 50
//        val kNThreads = 4
//        val kWaitInSec = 180
//
//        // createDocs2 returns synchronized List.
//        val docIDs = createDocs(kNDocs, "Create")
//        assertEquals(kNDocs, docIDs.size)
//
//        // concurrently creates documents
//        concurrentValidator(kNThreads, kWaitInSec,
//            Callback { threadIndex: Int -> readDocs(docIDs, kNRounds) })
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(CouchbaseLiteException::class)
//    fun testConcurrentReadInBatch() {
//        val kNDocs = 5
//        val kNRounds = 50
//        val kNThreads = 4
//        val kWaitInSec = 180
//
//        // createDocs2 returns synchronized List.
//        val docIDs = createDocs(kNDocs, "Create")
//        assertEquals(kNDocs, docIDs.size)
//
//        // concurrently creates documents
//        concurrentValidator(
//            kNThreads,
//            kWaitInSec,
//            Callback { threadIndex: Int ->
//                try {
//                    baseTestDb.inBatch { readDocs(docIDs, kNRounds) }
//                } catch (e: CouchbaseLiteException) {
//                    fail()
//                }
//            }
//        )
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class, CouchbaseLiteException::class)
//    fun testConcurrentReadAndUpdate() {
//        val kNDocs = 5
//        val kNRounds = 50
//
//        // createDocs2 returns synchronized List.
//        val docIDs = createDocs(kNDocs, "Create")
//        assertEquals(kNDocs, docIDs.size)
//
//        // Read:
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread("testConcurrentReadAndUpdate-1", latch1) {
//            readDocs(docIDs, kNRounds)
//        }
//
//        // Update:
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        val tag = "Update"
//        testOnNewThread("testConcurrentReadAndUpdate-2", latch2) {
//            assertTrue(updateDocs(docIDs, kNRounds, tag))
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//        verifyByTagName(tag, kNDocs)
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class, CouchbaseLiteException::class)
//    fun testConcurrentDelete() {
//        val kNDocs = 100
//
//        // createDocs2 returns synchronized List.
//        val docIDs = createDocs(kNDocs, "Create")
//        assertEquals(kNDocs.toLong(), docIDs.size.toLong())
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentDelete-1",
//            latch1
//        ) {
//            for (docID in docIDs) {
//                try {
//                    val doc =
//                        baseTestDb.getDocument(docID)
//                    if (doc != null) {
//                        baseTestDb.delete(doc)
//                    }
//                } catch (e: CouchbaseLiteException) {
//                    fail()
//                }
//            }
//        }
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentDelete-2",
//            latch2
//        ) {
//            for (docID in docIDs) {
//                try {
//                    val doc =
//                        baseTestDb.getDocument(docID)
//                    if (doc != null) {
//                        baseTestDb.delete(doc)
//                    }
//                } catch (e: CouchbaseLiteException) {
//                    fail()
//                }
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//        assertEquals(0, baseTestDb.count)
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class, CouchbaseLiteException::class)
//    fun testConcurrentPurge() {
//        val nDocs = 100
//
//        // createDocs returns synchronized List.
//        val docIDs = createDocs(nDocs, "Create")
//        assertEquals(nDocs.toLong(), docIDs.size.toLong())
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentPurge-1",
//            latch1
//        ) {
//            for (docID in docIDs) {
//                val doc = baseTestDb.getDocument(docID)
//                if (doc != null) {
//                    try {
//                        baseTestDb.purge(doc)
//                    } catch (e: CouchbaseLiteException) {
//                        assertEquals(404, e.getCode().toLong())
//                    }
//                }
//            }
//        }
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentPurge-2",
//            latch2
//        ) {
//            for (docID in docIDs) {
//                val doc = baseTestDb.getDocument(docID)
//                if (doc != null) {
//                    try {
//                        baseTestDb.purge(doc)
//                    } catch (e: CouchbaseLiteException) {
//                        assertEquals(404, e.getCode().toLong())
//                    }
//                }
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//        assertEquals(0, baseTestDb.count)
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class)
//    fun testConcurrentCreateAndCloseDB() {
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndCloseDB-1",
//            latch1
//        ) {
//            try {
//                createDocs(100, "Create1")
//            } catch (e: CouchbaseLiteException) {
//                if (!e.getDomain()
//                        .equals(CBLError.Domain.CBLITE) || e.getCode() != CBLError.Code.NOT_OPEN
//                ) {
//                    throw java.lang.AssertionError("Unrecognized exception", e)
//                }
//            } // db not open
//            catch (ignore: IllegalStateException) {
//            }
//        }
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndCloseDB-2",
//            latch2
//        ) {
//            try {
//                baseTestDb.close()
//            } catch (e: CouchbaseLiteException) {
//                fail()
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class)
//    fun testConcurrentCreateAndDeleteDB() {
//        val kNDocs = 100
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        val tag1 = "Create1"
//        testOnNewThread(
//            "testConcurrentCreateAndDeleteDB-1",
//            latch1
//        ) {
//            try {
//                createDocs(kNDocs, tag1)
//            } catch (e: CouchbaseLiteException) {
//                if (!e.getDomain()
//                        .equals(CBLError.Domain.CBLITE) || e.getCode() != CBLError.Code.NOT_OPEN
//                ) {
//                    fail()
//                }
//            } // db not open
//            catch (ignore: IllegalStateException) {
//            }
//        }
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndDeleteDB-2",
//            latch2
//        ) {
//            try {
//                baseTestDb.delete()
//            } catch (e: CouchbaseLiteException) {
//                fail()
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class)
//    fun testConcurrentCreateAndCompactDB() {
//        val kNDocs = 100
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndCompactDB-1",
//            latch1
//        ) {
//            try {
//                createDocs(kNDocs, "Create1")
//            } catch (e: CouchbaseLiteException) {
//                if (!e.getDomain()
//                        .equals(CBLError.Domain.CBLITE) || e.getCode() != CBLError.Code.NOT_OPEN
//                ) {
//                    fail()
//                }
//            }
//        }
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndCompactDB-2",
//            latch2
//        ) {
//            try {
//                assertTrue(baseTestDb.performMaintenance(MaintenanceType.COMPACT))
//            } catch (e: CouchbaseLiteException) {
//                fail()
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(Exception::class)
//    fun testConcurrentCreateAndCreateIndexDB() {
//        loadJSONResource("sentences.json")
//        val kNDocs = 100
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndCreateIndexDB-1",
//            latch1
//        ) {
//            try {
//                createDocs(kNDocs, "Create1")
//            } catch (e: CouchbaseLiteException) {
//                if (!e.getDomain()
//                        .equals(CBLError.Domain.CBLITE) || e.getCode() != CBLError.Code.NOT_OPEN
//                ) {
//                    fail()
//                }
//            }
//        }
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        testOnNewThread(
//            "testConcurrentCreateAndCreateIndexDB-2",
//            latch2
//        ) {
//            try {
//                val index: Index =
//                    IndexBuilder.fullTextIndex(
//                        FullTextIndexItem.property("sentence")
//                    )
//                baseTestDb.createIndex("sentence", index)
//            } catch (e: CouchbaseLiteException) {
//                fail()
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class)
//    fun testBlockDatabaseChange() {
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        baseTestDb.addChangeListener(testSerialExecutor) { change -> latch2.countDown() }
//        testOnNewThread(
//            "testBlockDatabaseChange",
//            latch1
//        ) {
//            try {
//                baseTestDb.save(MutableDocument("doc1"))
//            } catch (e: CouchbaseLiteException) {
//                fail()
//            }
//        }
//        assertTrue(latch1.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        assertTrue(latch2.await(TIMEOUT, java.util.concurrent.TimeUnit.SECONDS))
//        checkForFailure()
//    }
//
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(java.lang.InterruptedException::class)
//    fun testBlockDocumentChange() {
//        val latch1: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        val latch2: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
//        val token = baseTestDb.addDocumentChangeListener("doc1") { change -> latch2.countDown() }
//        try {
//            testOnNewThread(
//                "testBlockDocumentChange",
//                latch1
//            ) {
//                try {
//                    baseTestDb.save(MutableDocument("doc1"))
//                } catch (e: CouchbaseLiteException) {
//                    fail()
//                }
//            }
//            assertTrue(
//                latch1.await(
//                    TIMEOUT,
//                    java.util.concurrent.TimeUnit.SECONDS
//                )
//            )
//            assertTrue(
//                latch2.await(
//                    TIMEOUT,
//                    java.util.concurrent.TimeUnit.SECONDS
//                )
//            )
//            checkForFailure()
//        } finally {
//            baseTestDb.removeChangeListener(token)
//        }
//    }
//
//    // https://github.com/couchbase/couchbase-lite-android/issues/1407
//    @Test
//    @ConcurrencyUnitTest
//    @Throws(Exception::class)
//    fun testQueryExecute() {
//        loadJSONResource("names_100.json")
//        val query: Query = QueryBuilder
//            .select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.sequence))
//            .from(DataSource.database(baseTestDb))
//        concurrentValidator(
//            10,
//            180,
//            Callback { threadIndex: Int ->
//                try {
//                    query.execute().use { rs ->
//                        val results = rs.allResults()
//                        assertEquals(100, results.size)
//                        assertEquals(baseTestDb.count, results.size.toLong())
//                    }
//                } catch (e: CouchbaseLiteException) {
//                    Report.log(LogLevel.ERROR, "Query Error", e)
//                    fail()
//                }
//            }
//        )
//    }
//
//    private fun createDocumentWithTag(tag: String): MutableDocument {
//        val doc = MutableDocument()
//
//        // Tag
//        doc.setValue("tag", tag)
//
//        // String
//        doc.setValue("firstName", "Daniel")
//        doc.setValue("lastName", "Tiger")
//
//        // Dictionary:
//        val address = MutableDictionary()
//        address.setValue("street", "1 Main street")
//        address.setValue("city", "Mountain View")
//        address.setValue("state", "CA")
//        doc.setValue("address", address)
//
//        // Array:
//        val phones = MutableArray()
//        phones.addValue("650-123-0001")
//        phones.addValue("650-123-0002")
//        doc.setValue("phones", phones)
//
//        // Date:
//        doc.setValue("updated", Clock.System.now())
//        return doc
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    private fun createDocs(nDocs: Int, tag: String): List<String> {
//        val docs: MutableList<String> =
//            java.util.Collections.synchronizedList<String>(java.util.ArrayList<String>(nDocs))
//        for (i in 0 until nDocs) {
//            val doc = createDocumentWithTag(tag)
//            docs.add(saveDocInBaseTestDb(doc).getId())
//        }
//        return docs
//    }
//
//    private fun updateDocs(docIds: List<String>, rounds: Int, tag: String): Boolean {
//        for (i in 1..rounds) {
//            for (docId in docIds) {
//                val d = baseTestDb.getDocument(docId)
//                val doc = d!!.toMutable()
//                doc.setValue("tag", tag)
//                val address = doc.getDictionary("address")
//                assertNotNull(address)
//                val street = "$i street."
//                address.setValue("street", street)
//                val phones = doc.getArray("phones")
//                assertNotNull(phones)
//                assertEquals(2, phones.count)
//                val phone = "650-000-${i.paddedString(4)}"
//                phones.setValue(0, phone)
//                doc.setValue("updated", Clock.System.now())
//                try {
//                    baseTestDb.save(doc)
//                } catch (e: CouchbaseLiteException) {
//                    return false
//                }
//            }
//        }
//        return true
//    }
//
//    private fun readDocs(docIDs: List<String>, rounds: Int) {
//        for (i in 1..rounds) {
//            for (docID in docIDs) {
//                val doc = baseTestDb.getDocument(docID)
//                assertNotNull(doc)
//                assertEquals(docID, doc.id)
//            }
//        }
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    private fun verifyByTagName(tag: String, block: VerifyBlock<Result>) {
//        QueryBuilder.select(SelectResult.expression(Meta.id))
//            .from(DataSource.database(baseTestDb))
//            .where(Expression.property("tag").equalTo(Expression.string(tag)))
//            .execute().use { rs ->
//                for ((n, result) in rs.withIndex()) {
//                    block.verify(n + 1, result)
//                }
//            }
//    }
//
//    @Throws(CouchbaseLiteException::class)
//    private fun verifyByTagName(tag: String, nRows: Int) {
//        val count: java.util.concurrent.atomic.AtomicInteger =
//            java.util.concurrent.atomic.AtomicInteger(0)
//        verifyByTagName(tag,
//            VerifyBlock<Result> { n: Int, result: Result? -> count.incrementAndGet() })
//        assertEquals(nRows.toLong(), count.toInt().toLong())
//    }
//
//    private fun concurrentValidator(nThreads: Int, waitSec: Int, callback: Callback) {
//        // setup
//        val threads: Array<java.lang.Thread?> = arrayOfNulls<java.lang.Thread>(nThreads)
//        val latches: Array<java.util.concurrent.CountDownLatch?> =
//            arrayOfNulls<java.util.concurrent.CountDownLatch>(nThreads)
//        for (i in 0 until nThreads) {
//            latches[i] = java.util.concurrent.CountDownLatch(1)
//            threads[i] = java.lang.Thread(
//                {
//                    try {
//                        callback.callback(i)
//                        latches[i].countDown()
//                    } catch (failure: java.lang.AssertionError) {
//                        Report.log(LogLevel.DEBUG, "Test failed", failure)
//                        testFailure.compareAndSet(null, failure)
//                    }
//                },
//                "Thread-$i"
//            )
//        }
//
//        // start
//        for (i in 0 until nThreads) {
//            threads[i].start()
//        }
//
//        // wait
//        for (i in 0 until nThreads) {
//            try {
//                assertTrue(
//                    latches[i].await(
//                        waitSec.toLong(),
//                        java.util.concurrent.TimeUnit.SECONDS
//                    )
//                )
//            } catch (e: java.lang.InterruptedException) {
//                fail()
//            }
//        }
//        checkForFailure()
//    }
//
//    private fun testOnNewThread(
//        threadName: String,
//        latch: java.util.concurrent.CountDownLatch,
//        test: () -> Unit
//    ) {
//        newTestThread(threadName, latch, test).start()
//    }
//
//    private fun newTestThread(
//        threadName: String,
//        latch: java.util.concurrent.CountDownLatch,
//        test: () -> Unit
//    ): java.lang.Thread {
//        return java.lang.Thread(java.lang.Runnable {
//            try {
//                test.run()
//            } catch (failure: java.lang.AssertionError) {
//                Report.log(LogLevel.DEBUG, "Test failed", failure)
//                testFailure.compareAndSet(null, failure)
//            } finally {
//                latch.countDown()
//            }
//        })
//    }
//
//    private fun checkForFailure() {
//        val failure: java.lang.AssertionError = testFailure.get()
//        if (failure != null) {
//            throw java.lang.AssertionError(failure)
//        }
//    }
//
//    companion object {
//        private const val TIMEOUT = 180L
//    }
//}
