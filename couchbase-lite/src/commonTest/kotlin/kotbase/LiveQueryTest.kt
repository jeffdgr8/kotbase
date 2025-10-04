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
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndIncrement
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class LiveQueryTest : BaseDbTest() {

    companion object {
        // Maximum delay for db change debounce from core is 500ms, happening when there's a rapid change within 250ms of
        // previous query run. Query run time and other delay factors cannot be accurately approximated. Thus, an acceptable
        // approximate for the time platform should wait if we expect to NOT get a callback is 500 * 2.
        // more details on core delay can be found in c4Observer.h
        private const val APPROXIMATE_CORE_DELAY_MS = 500L * 2
        private const val KEY = "number"
    }

    /**
     * When a query observer is first registered, the query should get notified
     */
    @Test
    fun testCreateBasicListener() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(KEY).greaterThanOrEqualTo(Expression.intValue(0)))
            .orderBy(Ordering.property(KEY).ascending())

        val latch = CountDownLatch(1)
        query.addChangeListener(testSerialCoroutineContext) { latch.countDown() }.use {
            assertTrue(latch.await(LONG_TIMEOUT_SEC.seconds))
        }
    }

    /**
     * When a second observer is registered, it should get call back after query done running
     * The first observer should NOT get notified when the second observer is created
     * When there's a db change, both observers should get notified in a tolerable amount of time
     */
    @Test
    fun testMultipleListeners() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(KEY).greaterThanOrEqualTo(Expression.intValue(0)))
            .orderBy(Ordering.property(KEY).ascending())
        val latch1 = arrayOf(CountDownLatch(1), CountDownLatch(1))
        val latch2 = arrayOf(CountDownLatch(1), CountDownLatch(1))

        val atmCount1 = AtomicInt(0)
        val atmCount2 = AtomicInt(0)

        query.addChangeListener(testSerialCoroutineContext) {
            latch1[atmCount1.fetchAndIncrement()].countDown()
        }.use {

            // listener 1 gets notified after observer subscribed
            assertTrue(latch1[0].await(LONG_TIMEOUT_SEC.seconds))
            query.addChangeListener(testSerialCoroutineContext) {
                latch2[atmCount2.fetchAndIncrement()].countDown()
            }.use {
                // listener 2 should get notified
                assertTrue(latch2[0].await(LONG_TIMEOUT_SEC.seconds))

                // creation of the second listener should not trigger first listener callback
                assertFalse(latch1[1].await(APPROXIMATE_CORE_DELAY_MS.milliseconds))

                createDocNumbered(11)

                // introducing change in database should trigger both listener callbacks
                assertTrue(latch1[1].await(LONG_TIMEOUT_SEC.seconds))
                assertTrue(latch2[1].await(LONG_TIMEOUT_SEC.seconds))
            }
        }
    }

    // When a result set is closed, we should still be able to introduce a change
    @Test
    fun testCloseResultsInLiveQueryListener() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))

        val atmCount = AtomicInt(0)
        val latches = arrayOf(CountDownLatch(1), CountDownLatch(1))

        query.addChangeListener(testSerialCoroutineContext) { change ->
            change.results!!.close()
            latches[atmCount.fetchAndIncrement()].countDown()
        }.use {
            createDocNumbered(10)
            assertTrue(latches[0].await(LONG_TIMEOUT_SEC.seconds))

            createDocNumbered(11)
            assertTrue(latches[1].await(LONG_TIMEOUT_SEC.seconds))
        }
    }

    /**
     * Two observers should have two independent result sets.
     * When two observers try to iterate through the result set,
     * values in that rs should not be skipped because of the other observer
     */
    @Test
    fun testIterateRSWith2Listeners() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))

        val latch1 = CountDownLatch(1)
        val latch2 = CountDownLatch(1)

        query.addChangeListener(testSerialCoroutineContext) { change ->
            // even if the other listener finishes running first and iterates through doc-11,
            // this listener should get an independent rs, thus iterates from the beginning, getting doc-11
            change.results?.use { rs ->
                val r = rs.next()
                if (r?.getString(0) == "doc-11") {
                    latch1.countDown()
                }
            }
        }.use {
            query.addChangeListener(testSerialCoroutineContext) { change ->
                // even if the other listener finishes running first and iterates through doc-11,
                // this listener should get an independent rs, thus iterates from the beginning, getting doc-11
                change.results?.use { rs ->
                    val r = rs.next()
                    if (r?.getString(0) == "doc-11") {
                        latch2.countDown()
                    }
                }
            }.use {
                createDocNumbered(11)

                // both listeners get notified after doc-11 is created in database
                // rs iterates through the correct value
                assertTrue(latch1.await(LONG_TIMEOUT_SEC.seconds))
                assertTrue(latch2.await(LONG_TIMEOUT_SEC.seconds))
            }
        }
    }

    // Changing query parameters should cause an update within tolerable time
    @Test
    fun testChangeParameters(): Unit = runBlocking {
        createDocNumbered(1)
        createDocNumbered(2)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(KEY).greaterThanOrEqualTo(Expression.parameter("VALUE")))
            .orderBy(Ordering.property(KEY).ascending())

        val latch = arrayOf(CountDownLatch(1), CountDownLatch(1), CountDownLatch(1))

        // count is used to get the next latch and also check size of rs
        val atmCount = AtomicInt(0)

        // VALUE is set to 2, we should expect that query will only get notification for doc 2, rs size is 1
        var params = Parameters()
        params.setInt("VALUE", 2)
        query.parameters = params

        query.addChangeListener(testSerialCoroutineContext) { change ->
            change.results?.use { rs ->
                //  query should only be notified 2 times:
                //  1. query first gets doc 2, the rs size is 1
                //  2. after param changes to 1, query gets a new rs that has doc 1 and 2, rs size is now 2
                //  query should not be notified when doc 0 is added to the db
                if (rs.allResults().size == atmCount.load() + 1) {
                    latch[atmCount.fetchAndIncrement()].countDown()
                }
            }
        }.use {
            assertTrue(latch[0].await(LONG_TIMEOUT_SEC.seconds))

            params = Parameters()
            params.setInt("VALUE", 1)
            query.parameters = params

            // VALUE changes to 1, query now gets a new rs for doc 1 and 2
            assertTrue(latch[1].await(LONG_TIMEOUT_SEC.seconds))

            // This doc does not meet the condition of the query, thus query should not get notified
            createDocNumbered(0)
            assertFalse(latch[2].await(APPROXIMATE_CORE_DELAY_MS.milliseconds))
        }
    }

    // CBL-2344: Live query may stop refreshing
    @Test
    fun testLiveQueryRefresh() = runBlocking {
        val latchHolder = AtomicReference(CountDownLatch(1))
        val resultsHolder = AtomicReference<List<Result>>(emptyList())

        createDocNumbered(10)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(KEY).greaterThan(Expression.intValue(0)))

        query.addChangeListener(testSerialCoroutineContext) { change ->
            resultsHolder.store(change.results!!.allResults())
            latchHolder.load().countDown()
        }.use {
            // this update should happen nearly instantaneously
            assertTrue(latchHolder.load().await(LONG_TIMEOUT_SEC.seconds))
            assertEquals(1, resultsHolder.load().size)

            // adding this document will trigger the query but since it does not meet the query
            // criteria, it will not produce a new result. The listener should not be called.
            // Wait for 2 full update intervals and a little bit more.
            latchHolder.store(CountDownLatch(1))
            createDocNumbered(0)
            assertFalse(latchHolder.load().await(APPROXIMATE_CORE_DELAY_MS.milliseconds))

            // adding this document should cause a call to the listener in not much more than an update interval
            latchHolder.store(CountDownLatch(1))
            createDocNumbered(11)
            assertTrue(latchHolder.load().await(LONG_TIMEOUT_SEC.seconds))
            assertEquals(2, resultsHolder.load().size)
        }
    }

    // CBL-4423: must close all live queries when closing a database
//    @Test
//    fun testLiveQueryOnDBClose() {
//        createDocNumbered(10)
//
//        val query = testDatabase.createQuery("SELECT _id FROM ${testCollection.fullName}")
//
//        val listeners = mutableListOf<QueryChangeListener>()
//        val tokens = mutableListOf<ListenerToken>()
//        for (i in 0..6) {
//            val listener: QueryChangeListener = {}
//            listeners.add(listener)
//            tokens.add(query.addChangeListener(listener))
//        }
//
//        assertEquals(listeners.size, query.liveCount())
//        for (token in tokens) {
//            assertTrue(query.isLive(token))
//        }
//
//        testDatabase.close()
//
//        assertEquals(0, query.liveCount())
//    }

    // create test docs
    // !!! Replace with standard save routine
    private fun createDocNumbered(i: Int) {
        val docID = "doc-$i"
        val doc = MutableDocument(docID)
        doc.setValue(KEY, i)
        saveDocInCollection(doc)
    }
}
