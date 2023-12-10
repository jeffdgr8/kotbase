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

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class QueryChangeTest : BaseQueryTest() {

    // https://github.com/couchbase/couchbase-lite-android/issues/1615
    @Test
    fun testRemoveQueryChangeListenerInCallback() = runBlocking {
        loadDocuments(10)

        val query: Query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.collection(testCollection))
            .where(Expression.property(TEST_DOC_SORT_KEY).lessThan(Expression.intValue(5)))

        val token = atomic<ListenerToken?>(null)
        val latch = CountDownLatch(1)
        val lock = SynchronizedObject()

        val listener: QueryChangeListener = listener@{ change ->
            val rs = change.results
            if (rs?.next() == null) return@listener
            synchronized(lock) {
                val t = token.getAndSet(null)
                t?.remove()
            }
            latch.countDown()
        }

        // Removing the listener while inside the listener itself needs be done carefully.
        // The listener might get called before query.addChangeListener(), below, returns.
        // If that happened "token" would not yet have been set and the test would not work.
        // Seizing a lock here guarantees that that can't happen.
        synchronized(lock) { token.value = query.addChangeListener(testSerialCoroutineContext, listener) }
        try { assertTrue(latch.await(STD_TIMEOUT_SEC.seconds)) }
        finally {
            val t = token.value
            t?.remove()
        }

        assertNull(token.value)
    }
}
