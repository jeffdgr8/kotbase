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

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class FlowTest : BaseReplicatorTest() {
    @Suppress("DEPRECATION")
    @Test
    fun testDatabaseChangeFlow() {
        val docIds = mutableListOf<String>()

        runBlocking {
            val latch = CountDownLatch(1)

            val collector = launch(Dispatchers.Default) {
                testDatabase.databaseChangeFlow(testSerialCoroutineContext)
                    .map {
                        assertEquals(testDatabase, it.database, "change on wrong db")
                        it.documentIDs
                    }
                    .onEach { ids ->
                        docIds.addAll(ids)
                        if (docIds.size >= 10) {
                            latch.countDown()
                        }
                    }
                    .catch {
                        latch.countDown()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(100)

                // make 10 db changes
                for (i in 0..9) {
                    val doc = MutableDocument("doc-${i}")
                    doc.setValue("type", "demo")
                    saveDocInCollection(doc, testDatabase.defaultCollection)
                }
            }

            assertTrue(latch.await(1.seconds))
            collector.cancel()
        }

        assertEquals(10, docIds.size)
        for (i in 0..9) {
            val id = "doc-${i}"
            assertTrue(docIds.contains(id), "missing $id")
        }
    }

    @Test
    fun testCollectionChangeFlow() {
        val docIds = mutableListOf<String>()

        runBlocking {
            val latch = CountDownLatch(1)

            val collector = launch(Dispatchers.Default) {
                testCollection.collectionChangeFlow(testSerialCoroutineContext)
                    .map {
                        assertEquals(testCollection, it.collection, "change on wrong collection")
                        it.documentIDs
                    }
                    .onEach { ids ->
                        docIds.addAll(ids)
                        if (docIds.size >= 10) {
                            latch.countDown()
                        }
                    }
                    .catch {
                        latch.countDown()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(100)

                // make 10 db changes
                for (i in 0..9) {
                    val doc = MutableDocument("doc-${i}")
                    doc.setValue("type", "demo")
                    saveDocInCollection(doc, testCollection)
                }
            }

            assertTrue(latch.await(1.seconds))
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
            val latch = CountDownLatch(1)

            val collector = launch(Dispatchers.Default) {
                testCollection.documentChangeFlow(docA.id, testSerialCoroutineContext)
                    .onEach { change ->
                        changes.add(change)
                        latch.countDown()
                    }
                    .catch {
                        latch.countDown()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(100)

                saveDocInCollection(docB)
                saveDocInCollection(docA)
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, changes.size)
        assertEquals(testCollection, changes[0].collection, "change on wrong collection")
        assertEquals(docA.id, changes[0].documentID, "change on wrong doc")
    }

    @Test
    fun testDocumentChangeFlowOnUpdate() {
        val changes = mutableListOf<DocumentChange>()

        var mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInCollection(mDocA)
        var mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInCollection(mDocB)

        runBlocking {
            val latch = CountDownLatch(1)

            val collector = launch(Dispatchers.Default) {
                testCollection.documentChangeFlow(docA.id, testSerialCoroutineContext)
                    .onEach { change ->
                        changes.add(change)
                        latch.countDown()
                    }
                    .catch {
                        latch.countDown()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(100)

                mDocB = docB.toMutable()
                mDocB.setValue("thewronganswer", 42)
                saveDocInCollection(mDocB)

                mDocA = docA.toMutable()
                mDocA.setValue("thewronganswer", 18)
                saveDocInCollection(mDocA)
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, changes.size)
        assertEquals(testCollection, changes[0].collection, "change on wrong collection")
        assertEquals(docA.id, changes[0].documentID, "change on wrong doc")
    }

    @Test
    fun testDocumentChangeFlowOnDelete() {
        val changes = mutableListOf<DocumentChange>()

        val mDocA = MutableDocument("A")
        mDocA.setValue("theanswer", 18)
        val docA = saveDocInCollection(mDocA)
        val mDocB = MutableDocument("B")
        mDocB.setValue("thewronganswer", 18)
        val docB = saveDocInCollection(mDocB)

        runBlocking {
            val latch = CountDownLatch(1)

            val collector = launch(Dispatchers.Default) {
                testCollection.documentChangeFlow(docA.id, testSerialCoroutineContext)
                    .onEach { change ->
                        changes.add(change)
                        latch.countDown()
                    }
                    .catch {
                        latch.countDown()
                        throw it
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait until the collector starts
                delay(100)

                testCollection.delete(docB)
                testCollection.delete(docA)
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, changes.size)
        assertEquals(testCollection, changes[0].collection, "change on wrong collection")
        assertEquals(mDocA.id, changes[0].documentID, "change on wrong doc")
    }

    @Test
    fun testQueryChangeFlow() {
        val allResults = mutableListOf<Any?>()

        val mDoc = MutableDocument("doc-1")
        mDoc.setValue("theanswer", 18)

        runBlocking {
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.collection(testCollection))
            val latch = CountDownLatch(1)

            val collector = launch(Dispatchers.Default) {
                query.queryChangeFlow(testSerialCoroutineContext)
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
                        if (allResults.isNotEmpty()) {
                            latch.countDown()
                        }
                    }
                    .collect()
            }

            launch(Dispatchers.Default) {
                // Hate this: wait for the collector to start
                delay(100)

                testCollection.save(mDoc)
            }

            assertTrue(latch.await(STD_TIMEOUT_SEC.seconds))
            collector.cancel()
        }

        assertEquals(1, allResults.size)
        assertEquals(mDoc.id, allResults[0])
    }
}
