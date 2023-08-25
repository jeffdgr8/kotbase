package kotbase

import kotbase.test.lockWithTimeout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class FlowTest : BaseReplicatorTest() {

    @Test
    fun testDatabaseChangeFlow() {
        val docIds = mutableListOf<String>()

        runBlocking {
            val mutex = Mutex(true)

            val collector = launch(Dispatchers.Default) {
                baseTestDb.databaseChangeFlow()
                    .map {
                        assertEquals(baseTestDb, it.database, "change on wrong db")
                        it.documentIDs
                    }
                    .onEach { ids ->
                        docIds.addAll(ids)
                        if (docIds.size >= 10) {
                            mutex.unlock()
                        }
                    }
                    .catch {
                        mutex.unlock()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(30L)

                // make 10 db changes
                for (i in 0..9) {
                    val doc = MutableDocument("doc-${i}")
                    doc.setValue("type", "demo")
                    saveDocInBaseTestDb(doc)
                }
            }

            assertTrue(mutex.lockWithTimeout(1.seconds))
            collector.cancel()
        }

        assertEquals(10, docIds.size)
        for (i in 0..9) {
            val id = "doc-${i}"
            assertTrue(docIds.contains(id), "missing $id")
        }
    }

    @Test
    fun testDocumentChangeFlowOnSave() {
        val changes = mutableListOf<DocumentChange>()

        val docA = MutableDocument("A")
        docA.setValue("theanswer", 18)
        val docB = MutableDocument("B")
        docB.setValue("thewronganswer", 18)

        runBlocking {
            val mutex = Mutex(true)

            val collector = launch(Dispatchers.Default) {
                baseTestDb.documentChangeFlow(docA.id)
                    .onEach { change ->
                        changes.add(change)
                        mutex.unlock()
                    }
                    .catch {
                        mutex.unlock()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(30L)

                saveDocInBaseTestDb(docB)
                saveDocInBaseTestDb(docA)
            }

            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, changes.size)
        assertEquals(baseTestDb, changes[0].database, "change on wrong db")
        assertEquals(docA.id, changes[0].documentID, "change on wrong doc")
    }

    @Test
    fun testDocumentChangeFlowOnUpdate() {
        val changes = mutableListOf<DocumentChange>()

        var mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInBaseTestDb(mDocA)
        var mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInBaseTestDb(mDocB)

        runBlocking {
            val mutex = Mutex(true)

            val collector = launch(Dispatchers.Default) {
                baseTestDb.documentChangeFlow(docA.id)
                    .onEach { change ->
                        changes.add(change)
                        mutex.unlock()
                    }
                    .catch {
                        mutex.unlock()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(30L)

                mDocB = docB.toMutable()
                mDocB.setValue("thewronganswer", 42)
                saveDocInBaseTestDb(mDocB)

                mDocA = docA.toMutable()
                mDocA.setValue("thewronganswer", 18)
                saveDocInBaseTestDb(mDocA)
            }

            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, changes.size)
        assertEquals(baseTestDb, changes[0].database, "change on wrong db")
        assertEquals(docA.id, changes[0].documentID, "change on wrong doc")
    }

    @Test
    fun testDocumentChangeFlowOnDelete() {
        val changes = mutableListOf<DocumentChange>()

        val mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInBaseTestDb(mDocA)
        val mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInBaseTestDb(mDocB)

        runBlocking {
            val mutex = Mutex(true)

            val collector = launch(Dispatchers.Default) {
                baseTestDb.documentChangeFlow(docA.id)
                    .onEach { change ->
                        changes.add(change)
                        mutex.unlock()
                    }
                    .catch {
                        mutex.unlock()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(30L)

                baseTestDb.delete(docB)
                baseTestDb.delete(docA)
            }

            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, changes.size)
        assertEquals(baseTestDb, changes[0].database, "change on wrong db")
        assertEquals(mDocA.id, changes[0].documentID, "change on wrong doc")
    }

    @Test
    fun testQueryChangeFlow() {
        val allResults = mutableListOf<Any?>()

        val mDoc = MutableDocument("doc-1")
        mDoc.setValue("theanswer", 18)

        runBlocking {
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(baseTestDb))
            val mutex = Mutex(true)

            val collector = launch(Dispatchers.Default) {
                query.queryChangeFlow()
                    .map { change ->
                        val err = change.error
                        if (err != null) {
                            throw err
                        }
                        change.results?.allResults()?.flatMap { it.toList() }
                    }
                    .onEach { v ->
                        if (v != null) {
                            allResults.addAll(v)
                        }
                        if (allResults.size > 0) {
                            mutex.unlock()
                        }
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(30L)

                baseTestDb.save(mDoc)
            }

            assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, allResults.size)
        assertEquals(mDoc.id, allResults[0])
    }
}
