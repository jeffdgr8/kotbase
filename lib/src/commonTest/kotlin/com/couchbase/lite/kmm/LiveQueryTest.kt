package com.couchbase.lite.kmm

import com.udobny.kmm.test.IgnoreIos
import com.udobny.kmm.use
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class LiveQueryTest : BaseDbTest() {

    /**
     * When a query observer is first registered, the query should get notified
     */
    @Test
    fun testCreateBasicListener() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property(KEY).greaterThanOrEqualTo(Expression.intValue(0)))
            .orderBy(Ordering.property(KEY).ascending())

        val mutex = Mutex(true)
        val token = query.addChangeListener {
            mutex.unlock()
        }
        try {
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex.lock()
            }
        } finally {
            query.removeChangeListener(token)
        }
    }

    /**
     * When a second observer is registered, it should get call back after query done running
     * The first observer should NOT get notified when the second observer is created
     * When there's a db change, both observers should get notified in a tolerable amount of time
     */
    @Ignore // TODO: Fails with 3.0, requires 3.1
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testMultipleListeners() = runBlocking {
        lateinit var token1: ListenerToken
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property(KEY).greaterThanOrEqualTo(Expression.intValue(0)))
            .orderBy(Ordering.property(KEY).ascending())
        val mutex1 = arrayOf(Mutex(true), Mutex(true))
        val mutex2 = arrayOf(Mutex(true), Mutex(true))

        val atmCount = arrayOf(atomic(0), atomic(0))

        try {
            token1 = query.addChangeListener {
                mutex1[atmCount[0].getAndIncrement()].unlock()
            }

            lateinit var token2: ListenerToken
            // listener 1 gets notified after observer subscribed
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex1[0].lock()
            }
            try {
                token2 = query.addChangeListener {
                    mutex2[atmCount[1].getAndIncrement()].unlock()
                }
                // listener 2 should get notified
                withTimeout(LONG_TIMEOUT_SEC.seconds) {
                    mutex2[0].lock()
                }

                // creation of the second listener should not trigger first listener callback
                assertFailsWith<TimeoutCancellationException> {
                    withTimeout(APPROXIMATE_CORE_DELAY_MS.milliseconds) {
                        mutex1[1].lock()
                    }
                }

                createDocNumbered(11)

                // introducing change in database should trigger both listener callbacks
                withTimeout(LONG_TIMEOUT_SEC.seconds) {
                    mutex1[1].lock()
                }
                withTimeout(LONG_TIMEOUT_SEC.seconds) {
                    mutex2[1].lock()
                }
            } finally {
                query.removeChangeListener(token2)
            }
        } finally {
            query.removeChangeListener(token1)
        }
    }

    // TODO: iOS dispatches to the main thread and hangs live query after one callback
    //  even though listener is added with background queue
    //  main loop isn't started by Kotlin test app
    @IgnoreIos
    // When a result set is closed, we should still be able to introduce a change
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testCloseResultsInLiveQueryListener() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))

        val atmCount = atomic(0)
        val mutexes = arrayOf(Mutex(true), Mutex(true))

        val token = query.addChangeListener { change ->
            change.results!!.close()
            mutexes[atmCount.getAndIncrement()].unlock()
        }
        try {
            createDocNumbered(10)
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutexes[0].lock()
            }

            createDocNumbered(11)
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutexes[1].lock()
            }
        } finally {
            query.removeChangeListener(token)
        }
    }

    /**
     * Two observers should have two independent result sets.
     * When two observers try to iterate through the result set,
     * values in that rs should not be skipped because of the other observer
     */
    @Ignore // TODO: Fails with 3.0, requires 3.1
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testIterateRSWith2Listeners() = runBlocking {
        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))

        val mutex1 = Mutex(true)
        val mutex2 = Mutex(true)

        val token = query.addChangeListener { change ->
            // even if the other listener finishes running first and iterates through doc-11,
            // this listener should get an independent rs, thus iterates from the beginning, getting doc-11
            change.results?.use { rs ->
                rs.next()?.let { r ->
                    if (r.getString(0) == "doc-11") {
                        mutex1.unlock()
                    }
                }
            }
        }
        val token1 = query.addChangeListener { change ->
            // even if the other listener finishes running first and iterates through doc-11,
            // this listener should get an independent rs, thus iterates from the beginning, getting doc-11
            change.results?.use { rs ->
                rs.next()?.let { r ->
                    if (r.getString(0) == "doc-11") {
                        mutex2.unlock()
                    }
                }
            }
        }
        try {
            createDocNumbered(11)

            // both listeners get notified after doc-11 is created in database
            // rs iterates through the correct value
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex1.lock()
            }
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex2.lock()
            }
        } finally {
            query.removeChangeListener(token)
            query.removeChangeListener(token1)
        }
    }

    // Changing query parameters should cause an update within tolerable time
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testChangeParameters(): Unit = runBlocking {
        createDocNumbered(1)
        createDocNumbered(2)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property(KEY).greaterThanOrEqualTo(Expression.parameter("VALUE")))
            .orderBy(Ordering.property(KEY).ascending())

        val mutex = arrayOf(Mutex(true), Mutex(true), Mutex(true))

        // count is used to get the next latch and also check size of rs
        val atmCount = atomic(0)

        // VALUE is set to 2, we should expect that query will only get notification for doc 2, rs size is 1
        var params = Parameters()
        params.setInt("VALUE", 2)
        query.parameters = params

        val token = query.addChangeListener { change ->
            change.results?.use { rs ->
                //  query should only be notified 2 times:
                //  1. query first gets doc 2, the rs size is 1
                //  2. after param changes to 1, query gets a new rs that has doc 1 and 2, rs size is now 2
                //  query should not be notified when doc 0 is added to the db
                if (rs.allResults().size == atmCount.value + 1) {
                    mutex[atmCount.getAndIncrement()].unlock()
                }
            }
        }
        try {
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex[0].lock()
            }

            params = Parameters()
            params.setInt("VALUE", 1)
            query.parameters = params

            // VALUE changes to 1, query now gets a new rs for doc 1 and 2
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutex[1].lock()
            }

            // This doc does not meet the condition of the query, thus query should not get notified
            createDocNumbered(0)
            assertFailsWith<TimeoutCancellationException> {
                withTimeout(APPROXIMATE_CORE_DELAY_MS.milliseconds) {
                    mutex[2].lock()
                }
            }
        } finally {
            query.removeChangeListener(token)
        }
    }

    // TODO: iOS dispatches to the main thread and hangs live query after one callback
    //  even though listener is added with background queue
    //  main loop isn't start by Kotlin test app
    @IgnoreIos
    // CBL-2344: Live query may stop refreshing
    @Test
    @Throws(CouchbaseLiteException::class)
    fun testLiveQueryRefresh() = runBlocking {
        val mutexHolder = atomic(Mutex(true))
        val resultsHolder = atomic<List<Result>>(emptyList())

        createDocNumbered(10)

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property(KEY).greaterThan(Expression.intValue(0)))

        val token = query.addChangeListener { change ->
            resultsHolder.value = change.results!!.allResults()
            mutexHolder.value.unlock()
        }

        try {
            // this update should happen nearly instantaneously
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutexHolder.value.lock()
            }
            assertEquals(1, resultsHolder.value.size)

            // adding this document will trigger the query but since it does not meet the query
            // criteria, it will not produce a new result. The listener should not be called.
            // Wait for 2 full update intervals and a little bit more.
            mutexHolder.value = Mutex(true)
            createDocNumbered(0)
            assertFailsWith<TimeoutCancellationException> {
                withTimeout(APPROXIMATE_CORE_DELAY_MS.milliseconds) {
                    mutexHolder.value.lock()
                }
            }

            // adding this document should cause a call to the listener in not much more than an update interval
            mutexHolder.value = Mutex(true)
            createDocNumbered(11)
            withTimeout(LONG_TIMEOUT_SEC.seconds) {
                mutexHolder.value.lock()
            }
            assertEquals(2, resultsHolder.value.size)
        } finally {
            query.removeChangeListener(token)
        }
    }

    // create test docs
    @Throws(CouchbaseLiteException::class)
    private fun createDocNumbered(i: Int) {
        val docID = "doc-$i"
        val doc = MutableDocument(docID)
        doc.setValue(KEY, i)
        saveDocInBaseTestDb(doc)
    }

    companion object {
        // Maximum delay for db change debounce from core is 500ms, happening when there's a rapid change within 250ms of
        // previous query run. Query run time and other delay factors cannot be accurately approximated. Thus, an acceptable
        // approximate for the time platform should wait if we expect to NOT get a callback is 500 * 2.
        // more details on core delay can be found in c4Observer.h
        private const val APPROXIMATE_CORE_DELAY_MS = 500L * 2
        private const val KEY = "number"
    }
}
