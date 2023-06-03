package kotbase

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertNull
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

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }

        synchronized(lock) { assertNull(token[0]) }
    }
}
