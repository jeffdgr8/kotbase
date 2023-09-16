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

import kotbase.internal.utils.Report
import kotbase.internal.utils.paddedString
import kotbase.test.lockWithTimeout
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Clock
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalStdlibApi::class)
class ConcurrencyTest : BaseDbTest() {

    internal fun interface Callback {
        fun callback(coroutineIndex: Int)
    }

    internal fun interface VerifyBlock<T> {
        fun verify(n: Int, result: T)
    }

    private val testFailure = atomic<AssertionError?>(null)

    @BeforeTest
    fun setUpConcurrencyTest() {
        testFailure.value = null
    }

    // TODO: native C fails sometimes
    //  AssertionError: Expected <50>, actual <49>.
    @Test
    fun testConcurrentCreate() = runBlocking {
        Database.log.console.level = LogLevel.DEBUG
        val kNDocs = 50
        val kNCoroutines = 4
        val kWaitInSec = 180

        // concurrently creates documents
        concurrentValidator(kNCoroutines, kWaitInSec) { coroutineIndex ->
            val tag = "tag-$coroutineIndex"
            try {
                createDocs(kNDocs, tag)
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        // validate stored documents
        for (i in 0 until kNCoroutines) {
            verifyByTagName("tag-$i", kNDocs)
        }
    }

    @Test
    fun testConcurrentCreateInBatch() = runBlocking {
        val kNDocs = 50
        val kNCoroutines = 4
        val kWaitInSec = 180

        // concurrently creates documents
        concurrentValidator(kNCoroutines, kWaitInSec) { coroutineIndex ->
            val tag = "tag-$coroutineIndex"
            try {
                baseTestDb.inBatch { createDocs(kNDocs, tag) }
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        checkForFailure()

        // validate stored documents
        for (i in 0 until kNCoroutines) {
            verifyByTagName("tag-$i", kNDocs)
        }
    }

    @Test
    fun testConcurrentUpdate() = runBlocking {
        // ??? Increasing number of threads causes crashes
        // (from Java SDK, increasing number of coroutines doesn't cause crash)
        val nDocs = 5
        val nCoroutines = 4

        // createDocs2 returns synchronized List.
        val docIDs = createDocs(nDocs, "Create")
        assertEquals(nDocs, docIDs.size)

        // concurrently updates documents
        concurrentValidator(nCoroutines, 600) { coroutineIndex ->
            val tag = "tag-$coroutineIndex"
            assertTrue(updateDocs(docIDs, 50, tag))
        }

        val count = atomic(0)
        for (i in 0 until nCoroutines) {
            verifyByTagName("tag-$i") { _, _ -> count.incrementAndGet() }
        }

        assertEquals(nDocs, count.value)
    }

    @Test
    fun testConcurrentRead() = runBlocking {
        val kNDocs = 5
        val kNRounds = 50
        val kNCoroutines = 4
        val kWaitInSec = 180

        // createDocs2 returns synchronized List.
        val docIDs = createDocs(kNDocs, "Create")
        assertEquals(kNDocs, docIDs.size)

        // concurrently creates documents
        concurrentValidator(kNCoroutines, kWaitInSec) {
            readDocs(docIDs, kNRounds)
        }
    }

    @Test
    fun testConcurrentReadInBatch() = runBlocking {
        val kNDocs = 5
        val kNRounds = 50
        val kNCoroutines = 4
        val kWaitInSec = 180

        // createDocs2 returns synchronized List.
        val docIDs = createDocs(kNDocs, "Create")
        assertEquals(kNDocs, docIDs.size)

        // concurrently creates documents
        concurrentValidator(kNCoroutines, kWaitInSec) {
            try {
                baseTestDb.inBatch { readDocs(docIDs, kNRounds) }
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }
    }

    @Test
    fun testConcurrentReadAndUpdate() = runBlocking {
        val kNDocs = 5
        val kNRounds = 50

        // createDocs2 returns synchronized List.
        val docIDs = createDocs(kNDocs, "Create")
        assertEquals(kNDocs, docIDs.size)

        // Read:
        val mutex1 = Mutex(true)
        testOnNewCoroutine("testConcurrentReadAndUpdate-1", mutex1) {
            readDocs(docIDs, kNRounds)
        }

        // Update:
        val mutex2 = Mutex(true)
        val tag = "Update"
        testOnNewCoroutine("testConcurrentReadAndUpdate-2", mutex2) {
            assertTrue(updateDocs(docIDs, kNRounds, tag))
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()

        verifyByTagName(tag, kNDocs)
    }

    @Test
    fun testConcurrentDelete() = runBlocking {
        val kNDocs = 100

        // createDocs2 returns synchronized List.
        val docIDs = createDocs(kNDocs, "Create")
        assertEquals(kNDocs, docIDs.size)

        val mutex1 = Mutex(true)
        testOnNewCoroutine("testConcurrentDelete-1", mutex1) {
            for (docID in docIDs) {
                try {
                    val doc = baseTestDb.getDocument(docID)
                    if (doc != null) {
                        baseTestDb.delete(doc)
                    }
                } catch (e: CouchbaseLiteException) {
                    fail()
                }
            }
        }

        val mutex2 = Mutex(true)
        testOnNewCoroutine("testConcurrentDelete-2", mutex2) {
            for (docID in docIDs) {
                try {
                    val doc = baseTestDb.getDocument(docID)
                    if (doc != null) {
                        baseTestDb.delete(doc)
                    }
                } catch (e: CouchbaseLiteException) {
                    fail()
                }
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()

        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testConcurrentPurge() = runBlocking {
        val nDocs = 100

        // createDocs returns synchronized List.
        val docIDs = createDocs(nDocs, "Create")
        assertEquals(nDocs, docIDs.size)

        val mutex1 = Mutex(true)
        testOnNewCoroutine("testConcurrentPurge-1", mutex1) {
            for (docID in docIDs) {
                val doc = baseTestDb.getDocument(docID)
                if (doc != null) {
                    try {
                        baseTestDb.purge(doc)
                    } catch (e: CouchbaseLiteException) {
                        assertEquals(404, e.code)
                    }
                }
            }
        }
        val mutex2 = Mutex(true)
        testOnNewCoroutine("testConcurrentPurge-2", mutex2) {
            for (docID in docIDs) {
                val doc = baseTestDb.getDocument(docID)
                if (doc != null) {
                    try {
                        baseTestDb.purge(doc)
                    } catch (e: CouchbaseLiteException) {
                        assertEquals(404, e.code)
                    }
                }
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()

        assertEquals(0, baseTestDb.count)
    }

    @Test
    fun testConcurrentCreateAndCloseDB() = runBlocking {
        val mutex1 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndCloseDB-1", mutex1) {
            try {
                createDocs(100, "Create1")
            } catch (e: CouchbaseLiteException) {
                if (e.domain != CBLError.Domain.CBLITE || e.code != CBLError.Code.NOT_OPEN) {
                    throw AssertionError("Unrecognized exception", e)
                }
            } catch (ignore: IllegalStateException) {
                // db not open
            }
        }

        val mutex2 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndCloseDB-2", mutex2) {
            try {
                baseTestDb.close()
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()
    }

    @Test
    fun testConcurrentCreateAndDeleteDB() = runBlocking {
        val kNDocs = 100

        val mutex1 = Mutex(true)
        val tag1 = "Create1"
        testOnNewCoroutine("testConcurrentCreateAndDeleteDB-1", mutex1) {
            try {
                createDocs(kNDocs, tag1)
            } catch (e: CouchbaseLiteException) {
                if (e.domain != CBLError.Domain.CBLITE || e.code != CBLError.Code.NOT_OPEN) {
                    fail()
                }
            } catch (ignore: IllegalStateException) {
                // db not open
            }
        }
        val mutex2 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndDeleteDB-2", mutex2) {
            try {
                baseTestDb.delete()
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()
    }

    @Test
    fun testConcurrentCreateAndCompactDB() = runBlocking {
        val kNDocs = 100

        val mutex1 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndCompactDB-1", mutex1) {
            try {
                createDocs(kNDocs, "Create1")
            } catch (e: CouchbaseLiteException) {
                if (e.domain != CBLError.Domain.CBLITE || e.code != CBLError.Code.NOT_OPEN) {
                    fail()
                }
            }
        }

        val mutex2 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndCompactDB-2", mutex2) {
            try {
                assertTrue(baseTestDb.performMaintenance(MaintenanceType.COMPACT))
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()
    }

    @Test
    fun testConcurrentCreateAndCreateIndexDB() = runBlocking {
        loadJSONResource("sentences.json")

        val kNDocs = 100

        val mutex1 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndCreateIndexDB-1", mutex1) {
            try {
                createDocs(kNDocs, "Create1")
            } catch (e: CouchbaseLiteException) {
                if (e.domain != CBLError.Domain.CBLITE || e.code != CBLError.Code.NOT_OPEN) {
                    fail()
                }
            }
        }

        val mutex2 = Mutex(true)
        testOnNewCoroutine("testConcurrentCreateAndCreateIndexDB-2", mutex2) {
            try {
                val index: Index =
                    IndexBuilder.fullTextIndex(
                        FullTextIndexItem.property("sentence")
                    )
                baseTestDb.createIndex("sentence", index)
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()
    }

    @Test
    fun testBlockDatabaseChange() = runBlocking {
        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)

        @Suppress("UNUSED_VARIABLE")
        val token = baseTestDb.addChangeListener { mutex2.unlock() }

        testOnNewCoroutine("testBlockDatabaseChange", mutex1) {
            try {
                baseTestDb.save(MutableDocument("doc1"))
            } catch (e: CouchbaseLiteException) {
                fail()
            }
        }

        assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
        assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
        checkForFailure()
    }

    @Test
    fun testBlockDocumentChange() = runBlocking {
        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)

        val token = baseTestDb.addDocumentChangeListener("doc1") { mutex2.unlock() }
        try {
            testOnNewCoroutine("testBlockDocumentChange", mutex1) {
                try {
                    baseTestDb.save(MutableDocument("doc1"))
                } catch (e: CouchbaseLiteException) {
                    fail()
                }
            }

            assertTrue(mutex1.lockWithTimeout(TIMEOUT.seconds))
            assertTrue(mutex2.lockWithTimeout(TIMEOUT.seconds))
            checkForFailure()
        } finally {
            baseTestDb.removeChangeListener(token)
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1407
    @Test
    fun testQueryExecute() = runBlocking {
        loadJSONResource("names_100.json")

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.sequence))
            .from(DataSource.database(baseTestDb))

        concurrentValidator(
            10,
            180
        ) {
            try {
                query.execute().use { rs ->
                    val results = rs.allResults()
                    assertEquals(100, results.size)
                    assertEquals(baseTestDb.count, results.size.toLong())
                }
            } catch (e: CouchbaseLiteException) {
                Report.log(LogLevel.ERROR, "Query Error", e)
                fail()
            }
        }
    }

    private fun createDocumentWithTag(tag: String): MutableDocument {
        val doc = MutableDocument()

        // Tag
        doc.setValue("tag", tag)

        // String
        doc.setValue("firstName", "Daniel")
        doc.setValue("lastName", "Tiger")

        // Dictionary:
        val address = MutableDictionary()
        address.setValue("street", "1 Main street")
        address.setValue("city", "Mountain View")
        address.setValue("state", "CA")
        doc.setValue("address", address)

        // Array:
        val phones = MutableArray()
        phones.addValue("650-123-0001")
        phones.addValue("650-123-0002")
        doc.setValue("phones", phones)

        // Date:
        doc.setValue("updated", Clock.System.now())

        return doc
    }

    private fun createDocs(nDocs: Int, tag: String): List<String> {
        val docs = mutableListOf<String>()
        for (i in 0 until nDocs) {
            val doc = createDocumentWithTag(tag)
            docs.add(saveDocInBaseTestDb(doc).id)
        }
        return docs
    }

    private fun updateDocs(docIds: List<String>, rounds: Int, tag: String): Boolean {
        for (i in 1..rounds) {
            for (docId in docIds) {
                val d = baseTestDb.getDocument(docId)!!
                val doc = d.toMutable()
                doc.setValue("tag", tag)

                val address = doc.getDictionary("address")
                assertNotNull(address)
                val street = "$i street."
                address.setValue("street", street)

                val phones = doc.getArray("phones")
                assertNotNull(phones)
                assertEquals(2, phones.count)
                val phone = "650-000-${i.paddedString(4)}"
                phones.setValue(0, phone)

                doc.setValue("updated", Clock.System.now())
                try {
                    baseTestDb.save(doc)
                } catch (e: CouchbaseLiteException) {
                    return false
                }
            }
        }
        return true
    }

    private fun readDocs(docIDs: List<String>, rounds: Int) {
        for (i in 1..rounds) {
            for (docID in docIDs) {
                val doc = baseTestDb.getDocument(docID)
                assertNotNull(doc)
                assertEquals(docID, doc.id)
            }
        }
    }

    private fun verifyByTagName(tag: String, block: VerifyBlock<Result>) {
        QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("tag").equalTo(Expression.string(tag)))
            .execute().use { rs ->
                rs.forEachIndexed { n, result ->
                    block.verify(n, result)
                }
            }
    }

    private fun verifyByTagName(tag: String, nRows: Int) {
        val count = atomic(0)
        verifyByTagName(tag) { _, _ -> count.incrementAndGet() }
        assertEquals(nRows, count.value)
    }

    private suspend fun concurrentValidator(nCoroutines: Int, waitSec: Int, callback: Callback) =
        coroutineScope {
            // setup
            val jobs = arrayOfNulls<Job>(nCoroutines)
            val mutexes = arrayOfNulls<Mutex>(nCoroutines)

            for (i in 0 until nCoroutines) {
                mutexes[i] = Mutex(true)
                jobs[i] = launch(
                    CoroutineName("Coroutine-$i") + Dispatchers.Default,
                    CoroutineStart.LAZY
                ) {
                    try {
                        callback.callback(i)
                        mutexes[i]!!.unlock()
                    } catch (failure: AssertionError) {
                        Report.log(LogLevel.DEBUG, "Test failed", failure)
                        testFailure.compareAndSet(null, failure)
                    }
                }
            }

            // start
            for (i in 0 until nCoroutines) {
                jobs[i]!!.start()
            }

            // wait
            for (i in 0 until nCoroutines) {
                assertTrue(mutexes[i]!!.lockWithTimeout(waitSec.seconds))
            }

            checkForFailure()
        }

    private fun CoroutineScope.testOnNewCoroutine(
        coroutineName: String,
        mutex: Mutex,
        test: () -> Unit
    ) {
        newTestCoroutine(coroutineName, mutex, test).start()
    }

    private fun CoroutineScope.newTestCoroutine(
        coroutineName: String,
        mutex: Mutex,
        test: () -> Unit
    ): Job {
        return launch(CoroutineName(coroutineName) + Dispatchers.Default) {
            try {
                test()
            } catch (failure: AssertionError) {
                Report.log(LogLevel.DEBUG, "Test failed", failure)
                testFailure.compareAndSet(null, failure)
            } finally {
                mutex.unlock()
            }
        }
    }

    private fun checkForFailure() {
        val failure = testFailure.value
        if (failure != null) {
            throw AssertionError(failure)
        }
    }

    companion object {
        private const val TIMEOUT = 180L
    }
}
