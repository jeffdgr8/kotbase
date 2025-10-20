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

import co.touchlab.stately.collections.ConcurrentMutableList
import kotbase.internal.utils.paddedString
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.CountDownLatch
import kotlinx.coroutines.sync.CyclicBarrier
import kotlin.time.Clock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

// ignore while tracking memory references
@Ignore
@OptIn(ExperimentalAtomicApi::class)
class ConcurrencyTest : BaseDbTest() {

    // TODO: native C fails sometimes
    //  AssertionError: Expected <50>, actual <49>.
    @Test
    fun testConcurrentCreate() = runBlocking {
        val nDocs = 50

        val copies = 4
        runConcurrentCopies(
            copies
        ) { id ->
            try {
                for (mDoc in createComplexTestDocs(nDocs, "TAG@CREATES-$id")) {
                    testCollection.save(mDoc)
                }
            }
            catch (e: CouchbaseLiteException) {
                // Cause isn't logged on native platforms...
                // https://youtrack.jetbrains.com/issue/KT-62794
                println("Cause:")
                println(e.message)
                println(e.stackTraceToString())
                throw AssertionError("Failed saving doc", e)
            }
        }

        // validate stored documents
        for (i in 0..<copies) { assertEquals(nDocs, countTaggedDocs("TAG@CREATES-$i")) }
    }

    @Test
    fun testConcurrentCreateInBatch() = runBlocking {
        val nDocs = 50

        val copies = 4
        runConcurrentCopies(
            copies
        ) { id ->
            try {
                for (mDoc in createComplexTestDocs(nDocs, "TAG@CREATESBATCH-$id")) {
                    testDatabase.inBatch { testCollection.save(mDoc) }
                }
            }
            catch (e: CouchbaseLiteException) {
                // Cause isn't logged on native platforms...
                // https://youtrack.jetbrains.com/issue/KT-62794
                println("Cause:")
                println(e.message)
                println(e.stackTraceToString())
                throw AssertionError("Failed saving doc in batch", e)
            }
        }

        // validate stored documents
        for (i in 0..<copies) {
            assertEquals(nDocs, countTaggedDocs("TAG@CREATESBATCH-$i"))
        }
    }

    @Test
    fun testConcurrentReads() = runBlocking {
        val docIDs = saveDocs(createComplexTestDocs(5, "TAG@READS"))
        runConcurrentCopies(4) { readDocs(docIDs, 50) }
    }

    @Test
    fun testConcurrentReadsInBatch() = runBlocking {
        val docIDs = saveDocs(createComplexTestDocs(5, "TAG@READSBATCH"))

        runConcurrentCopies(4) {
            try {
                testDatabase.inBatch { readDocs(docIDs, 50) }
            } catch (e: CouchbaseLiteException) {
                // Cause isn't logged on native platforms...
                // https://youtrack.jetbrains.com/issue/KT-62794
                println("Cause:")
                println(e.message)
                println(e.stackTraceToString())
                throw AssertionError("Failed reading docs in batch", e)
            }
        }
    }

    // ??? Increasing the number of threads in this test causes crashes
    @Test
    fun testConcurrentUpdates() = runBlocking {
        val docIDs = saveDocs(createComplexTestDocs(5, "TAG@UPDATES"))

        val copies = 4
        runConcurrentCopies(copies) { id -> updateDocs(docIDs, 50, "TAG@UPDATED-$id") }

        var count = 0
        for (i in 0..<copies) { count += countTaggedDocs("TAG@UPDATED-$i") }

        assertEquals(docIDs.size, count)
    }

    @Test
    fun testConcurrentDeletes() = runBlocking {
        val docIDs = saveDocs(createComplexTestDocs(100, "TAG@DELETES"))

        runConcurrently(
            name = "delete",
            task1 = {
                for (docID in docIDs) {
                    try {
                        val doc = testCollection.getDocument(docID)
                        if (doc != null) {
                            testCollection.delete(doc)
                        }
                    }
                    catch (e: CouchbaseLiteException) {
                        // Cause isn't logged on native platforms...
                        // https://youtrack.jetbrains.com/issue/KT-62794
                        println("Cause:")
                        println(e.message)
                        println(e.stackTraceToString())
                        throw AssertionError("Failed deleting doc: $docID", e)
                    }
                }
            },
            task2 = {
                for (docID in docIDs) {
                    try {
                        val doc = testCollection.getDocument(docID)
                        if (doc != null) { testCollection.delete(doc) }
                    }
                    catch (e: CouchbaseLiteException) {
                        // Cause isn't logged on native platforms...
                        // https://youtrack.jetbrains.com/issue/KT-62794
                        println("Cause:")
                        println(e.message)
                        println(e.stackTraceToString())
                        throw AssertionError("Failed deleting doc: $docID", e)
                    }
                }
            }
        )
        assertEquals(0, testCollection.count)
    }

    @Test
    fun testConcurrentPurges() = runBlocking {
        val docIDs = saveDocs(createComplexTestDocs(100, "TAG@PURGES"))

        runConcurrently(
            name = "purge",
            task1 = {
                for (docID in docIDs) {
                    try {
                        val doc = testCollection.getDocument(docID)
                        if (doc != null) { testCollection.purge(doc) }
                    }
                    catch (e: CouchbaseLiteException) {
                        if (e.code != 404) {
                            // Cause isn't logged on native platforms...
                            // https://youtrack.jetbrains.com/issue/KT-62794
                            println("Cause:")
                            println(e.message)
                            println(e.stackTraceToString())
                            throw AssertionError("Failed purging doc: $docID", e)
                        }
                    }
                }
            },
            task2 = {
                for (docID in docIDs) {
                    try {
                        val doc = testCollection.getDocument(docID)
                        if (doc != null) { testCollection.purge(doc) }
                    }
                    catch (e: CouchbaseLiteException) {
                        if (e.code != 404) {
                            // Cause isn't logged on native platforms...
                            // https://youtrack.jetbrains.com/issue/KT-62794
                            println("Cause:")
                            println(e.message)
                            println(e.stackTraceToString())
                            throw AssertionError("Failed purging doc: $docID", e)
                        }
                    }
                }
            }
        )
        assertEquals(0, testCollection.count)
    }

    @Test
    fun testConcurrentReadWhileUpdate() = runBlocking {
        val docIDs = saveDocs(createComplexTestDocs(5, "TAG@READ&UPDATE"))
        runConcurrently(
            name = "readWhileUpdate",
            task1 = { readDocs(docIDs, 50) },
            task2 = { updateDocs(docIDs, 50, "TAG@READ&UPDATED") }
        )
        assertEquals(docIDs.size, countTaggedDocs("TAG@READ&UPDATED"))
    }

    @Test
    fun testConcurrentCreateWhileCloseDB() = runBlocking {
        val docs = createComplexTestDocs(100, "TAG@CLOSEDB")
        runConcurrently(
            name = "createWhileCloseD",
            task1 = {
                delay() // wait for other task to get busy...
                closeDb(testDatabase)
            },
            task2 = {
                for (mDoc in docs) {
                    try {
                        testCollection.save(mDoc)
                    } catch (e: CouchbaseLiteException) {
                        if (e.domain == CBLError.Domain.CBLITE && e.code == CBLError.Code.NOT_OPEN) {
                            break
                        }
                        // Cause isn't logged on native platforms...
                        // https://youtrack.jetbrains.com/issue/KT-62794
                        println("Cause:")
                        println(e.message)
                        println(e.stackTraceToString())
                        throw AssertionError("Failed saving document: $mDoc", e)
                    }
                }
            }
        )
    }

    @Test
    fun testConcurrentCreateWhileDeleteDB() = runBlocking {
        val docs = createComplexTestDocs(100, "TAG@DELETEDB")

        runConcurrently(
            name = "createWhileDeleteDb",
            task1 = {
                delay() // wait for other task to get busy...
                deleteDb(testDatabase)
            },
            task2 = {
                for (mDoc in docs) {
                    try {
                        testCollection.save(mDoc)
                    } catch (e: CouchbaseLiteException) {
                        if (e.domain == CBLError.Domain.CBLITE && e.code == CBLError.Code.NOT_OPEN) {
                            break
                        }
                        // Cause isn't logged on native platforms...
                        // https://youtrack.jetbrains.com/issue/KT-62794
                        println("Cause:")
                        println(e.message)
                        println(e.stackTraceToString())
                        throw AssertionError("Failed saving document: $mDoc", e)
                    }
                }
            }
        )
    }

    @Test
    fun testConcurrentCreateWhileCompactDB() = runBlocking {
        val docs = createComplexTestDocs(100, "TAG@COMPACTDB")

        runConcurrently(
            name = "createAndCompactDb@1",
            task1 = {
                try {
                    delay() // wait for other task to get busy...
                    if (!testDatabase.performMaintenance(MaintenanceType.COMPACT)) {
                        throw CouchbaseLiteException("Compaction failed")
                    }
                }
                catch (e: CouchbaseLiteException) {
                    // Cause isn't logged on native platforms...
                    // https://youtrack.jetbrains.com/issue/KT-62794
                    println("Cause:")
                    println(e.message)
                    println(e.stackTraceToString())
                    throw AssertionError("Failed compacting database", e)
                }
            },
            task2 = {
                for (doc in docs) {
                    try { testCollection.save(doc) }
                    catch (e: CouchbaseLiteException) {
                        if (e.domain == CBLError.Domain.CBLITE && e.code == CBLError.Code.NOT_OPEN) {
                            break
                        }
                        // Cause isn't logged on native platforms...
                        // https://youtrack.jetbrains.com/issue/KT-62794
                        println("Cause:")
                        println(e.message)
                        println(e.stackTraceToString())
                        throw AssertionError("Failed saving document: $doc", e)
                    }
                }
            }
        )
    }

    @Test
    fun testConcurrentCreateWhileIndexDB() = runBlocking {
        loadJSONResourceIntoCollection("sentences.json")

        val docs = createComplexTestDocs(100, "TAG@INDEX")

        runConcurrently(
            name = "CreateWhileIndex",
            task1 = {
                try {
                    testCollection.createIndex(
                        "sentence",
                        IndexBuilder.fullTextIndex(FullTextIndexItem.property("sentence"))
                    )
                }
                catch (e: CouchbaseLiteException) {
                    // Cause isn't logged on native platforms...
                    // https://youtrack.jetbrains.com/issue/KT-62794
                    println("Cause:")
                    println(e.message)
                    println(e.stackTraceToString())
                    throw AssertionError("Failed creating index", e)
                }
            },
            task2 = { saveDocs(docs) }
        )
    }

    @Test
    @Throws(CancellationException::class)
    fun testBlockDatabaseChange() = runBlocking {
        val latch = CountDownLatch(1)
        val error = AtomicReference<Exception?>(null)

        testCollection.addChangeListener(testSerialCoroutineContext) { latch.countDown() }.use {
            launch(testSerialCoroutineContext) {
                try { testCollection.save(MutableDocument()) }
                catch (e: Exception) { error.compareAndSet(null, e) }
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        }

        val e = error.load()
        if (e != null) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Error saving document", e)
        }
    }

    @Test
    fun testBlockDocumentChange() = runBlocking {
        val mDoc = MutableDocument()

        val latch = CountDownLatch(1)
        val error = AtomicReference<Exception?>(null)

        testCollection.addDocumentChangeListener(mDoc.id, testSerialCoroutineContext) { latch.countDown() }.use {
            launch(testSerialCoroutineContext) {
                try { testCollection.save(mDoc) }
                catch (e: Exception) { error.compareAndSet(null, e) }
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
        }

        val e = error.load()
        if (e != null) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Error saving document", e)
        }
    }

    // https://github.com/couchbase/couchbase-lite-android/issues/1407
    @Test
    fun testQueryExecute() = runBlocking {
        loadJSONResourceIntoCollection("names_100.json")

        val query = QueryBuilder.select(SelectResult.expression(Meta.id), SelectResult.expression(Meta.sequence))
            .from(DataSource.collection(testCollection))

        val nResults: MutableList<Int> = ConcurrentMutableList()
        runConcurrentCopies(10) {
            try { query.execute().use { rs -> nResults.add(rs.allResults().size) } }
            catch (e: CouchbaseLiteException) {
                // Cause isn't logged on native platforms...
                // https://youtrack.jetbrains.com/issue/KT-62794
                println("Cause:")
                println(e.message)
                println(e.stackTraceToString())
                throw AssertionError("Failed executing query", e)
            }
        }

        assertEquals(10, nResults.size)
        for (n in nResults) { assertEquals(testCollection.count, n.toLong()) }
    }

    private fun saveDocs(mDocs: List<MutableDocument>): List<String> {
        return try { ConcurrentMutableList<String>().apply { saveDocsInCollection(mDocs).forEach { add(it.id) } } }
        catch (e: Exception) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Failed saving documents", e)
        }
    }

    private fun updateDocs(docIds: List<String>, rounds: Int, tag: String) {
        for (i in 1..rounds) {
            for (docId in docIds) {
                val mDoc = try { testCollection.getDocument(docId)!!.toMutable() }
                catch (e: CouchbaseLiteException) {
                    // Cause isn't logged on native platforms...
                    // https://youtrack.jetbrains.com/issue/KT-62794
                    println("Cause:")
                    println(e.message)
                    println(e.stackTraceToString())
                    throw AssertionError("Failed getting document: $docId", e)
                }

                mDoc.setValue(TEST_DOC_TAG_KEY, tag)

                val address = mDoc.getDictionary("address")
                assertNotNull(address)
                val street = "$i street."
                address.setValue("street", street)

                val phones = mDoc.getArray("phones")
                assertNotNull(phones)
                assertEquals(2, phones.count())
                val phone = "650-000-${i.paddedString(4)}"
                phones.setValue(0, phone)

                mDoc.setValue("updated", Clock.System.now())
                try { testCollection.save(mDoc) }
                catch (e: CouchbaseLiteException) {
                    // Cause isn't logged on native platforms...
                    // https://youtrack.jetbrains.com/issue/KT-62794
                    println("Cause:")
                    println(e.message)
                    println(e.stackTraceToString())
                    throw AssertionError("Failed saving document: $docId", e)
                }
            }
        }
    }

    private fun readDocs(docIDs: List<String>, rounds: Int) {
        for (i in 1..rounds) {
            for (docID in docIDs) {
                val doc = try { testCollection.getDocument(docID) }
                catch (e: CouchbaseLiteException) {
                    // Cause isn't logged on native platforms...
                    // https://youtrack.jetbrains.com/issue/KT-62794
                    println("Cause:")
                    println(e.message)
                    println(e.stackTraceToString())
                    throw AssertionError("Failed reading document: $docID", e)
                }
                assertNotNull(doc)
                assertEquals(docID, doc.id)
            }
        }
    }

    private fun countTaggedDocs(tag: String): Int {
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_TAG_KEY).equalTo(Expression.string(tag)))
        query.execute().use { rs -> return rs.allResults().size }
    }

    private suspend fun runConcurrently(
        name: String,
        task1: suspend () -> Unit,
        task2: suspend () -> Unit
    ) = coroutineScope {
        val barrier = CyclicBarrier(2)
        val latch = CountDownLatch(2)
        val error = AtomicReference<Throwable?>(null)

        createTestCoroutines("$name@1", 1, barrier, latch, error) { task1() }
        createTestCoroutines("$name@2", 1, barrier, latch, error) { task2() }

        var ok = false
        try { ok = latch.await(STD_TIMEOUT_SEC.seconds) }
        catch (_: CancellationException) { }

        checkForFailure(error)

        assertTrue(ok)
    }

    private suspend fun runConcurrentCopies(nThreads: Int, task: (Int) -> Unit) = coroutineScope {
        val barrier = CyclicBarrier(nThreads)
        val latch = CountDownLatch(nThreads)
        val error = AtomicReference<Throwable?>(null)

        createTestCoroutines("Concurrency-test", nThreads, barrier, latch, error, task)

        // wait
        assertTrue(latch.await(LONG_TIMEOUT_SEC.seconds))

        checkForFailure(error)
    }

    private fun CoroutineScope.createTestCoroutines(
        name: String,
        nThreads: Int,
        barrier: CyclicBarrier,
        latch: CountDownLatch,
        error: AtomicReference<Throwable?>,
        task: suspend (Int) -> Unit
    ) {
        for (i in 0..<nThreads) {
            val coroutineName = "$name-$i"
            launch(CoroutineName(coroutineName)) {
                try {
                    barrier.await()
                    task(i)
                }
                catch (e: Exception) {
                    error.compareAndSet(
                        null,
                        AssertionError("Unexpected error in test on thread $coroutineName", e)
                    )
                }
                finally {
                    latch.countDown()
                }
            }
        }
    }

    private fun checkForFailure(error: AtomicReference<Throwable?>) {
        val err = error.load()
        if (err is AssertionError) { throw err }
        if (err != null) { throw AssertionError("Exception thrown in test", err) }
    }

    private suspend fun delay() {
        try { delay(2) }
        catch (_: CancellationException) { }
    }
}
