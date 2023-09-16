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

import kotbase.test.lockWithTimeout
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class QueryChangeTest : BaseQueryTest() {

    // https://github.com/couchbase/couchbase-lite-android/issues/1615
    @Test
    @Throws(Exception::class)
    fun testRemoveQueryChangeListenerInCallback() = runBlocking {
        loadNumberedDocs(10)

        val query: Query = QueryBuilder
            .select(SelectResult.expression(Meta.id))
            .from(DataSource.database(baseTestDb))
            .where(Expression.property("number1").lessThan(Expression.intValue(5)))

        val token = arrayOfNulls<ListenerToken>(1)
        val lock = SynchronizedObject()

        // Removing the listener while inside the listener itself needs be done carefully.
        // The change handler might get called from the executor thread before query.addChangeListener() returns.
        val mutex = Mutex(true)
        val listener = { change: QueryChange ->
            val rs = change.results
            if (rs?.next() != null) {
                synchronized(lock) {
                    query.removeChangeListener(token[0]!!)
                    token[0] = null
                }
            }
            mutex.unlock()
        }

        synchronized(lock) { token[0] = query.addChangeListener(listener) }

        assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))

        synchronized(lock) { assertNull(token[0]) }
    }
}
