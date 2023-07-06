package kotbase

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

class CoroutineTest : BaseDbTest() {

    private fun testOnCoroutineContext(
        addListener: (context: CoroutineContext, work: suspend () -> Unit) -> Unit,
        change: () -> Unit
    ) = runBlocking {
        val mutex = Mutex(true)
        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        val testContext = newSingleThreadContext("test-context-thread") + CoroutineName("test-context")
        addListener(testContext) {
            checkContext(testContext)
            mutex.unlock()
        }
        change()
        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }
    }

    private suspend fun checkContext(context: CoroutineContext) {
        assertEquals(context[CoroutineDispatcher], coroutineContext[CoroutineDispatcher])
        assertEquals(context[CoroutineName], coroutineContext[CoroutineName])
    }

    fun testCoroutineCanceled(
        addListener: (context: CoroutineContext, work: suspend () -> Unit) -> ListenerToken,
        change: () -> Unit,
        removeListener: (token: ListenerToken) -> Unit
    ) = runBlocking {
        val started = Mutex(true)
        val canceled = Mutex(true)
        val token = addListener(Dispatchers.Default) {
            try {
                started.unlock()
                delay(1000)
                fail("Coroutine should have been canceled")
            } catch (e: CancellationException) {
                canceled.unlock()
                throw e
            }
        }
        change()
        withTimeout(STD_TIMEOUT_SEC.seconds) {
            started.lock()
        }
        removeListener(token)
        withTimeout(STD_TIMEOUT_SEC.seconds) {
            canceled.lock()
        }
    }

    private fun testCoroutineScopeListenerRemoved(
        addListener: (scope: CoroutineScope, work: () -> Unit) -> Unit,
        listenedChange: () -> Unit,
        notListenedChange: () -> Unit
    ) = runBlocking {
        val mutex = Mutex(true)
        val scope = CoroutineScope(SupervisorJob())
        val count = atomic(0)
        addListener(scope) {
            count.value++
            mutex.unlock()
        }
        listenedChange()
        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }
        scope.cancel()
        notListenedChange()
        delay(100) // give listener time to be called if still listening
        assertEquals(1, count.value)
    }

    // DatabaseChange

    @Test
    fun testDatabaseChangeOnCoroutineContext() {
        testOnCoroutineContext(
            addListener = { context, work ->
                baseTestDb.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testDatabaseChangeCoroutineCanceled() {
        testCoroutineCanceled(
            addListener = { context, work ->
                baseTestDb.addChangeListener(Dispatchers.Default) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                baseTestDb.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testDatabaseChangeCoroutineScopeListenerRemoved() {
        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                baseTestDb.addChangeListener(scope) {
                    work()
                }
            },
            listenedChange = {
                saveDocInBaseTestDb(MutableDocument("withListener"))
            },
            notListenedChange = {
                saveDocInBaseTestDb(MutableDocument("noListener"))
            }
        )
    }

    // DocumentChange

    @Test
    fun testDocumentChangeOnCoroutineContext() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInBaseTestDb(doc)

        testOnCoroutineContext(
            addListener = { context, work ->
                baseTestDb.addDocumentChangeListener(id, context) {
                    work()
                }
            },
            change = {
                doc.setString("property", "changed value")
                saveDocInBaseTestDb(doc)
            }
        )
    }

    @Test
    fun testDocumentChangeCoroutineCanceled() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInBaseTestDb(doc)

        testCoroutineCanceled(
            addListener = { context, work ->
                baseTestDb.addDocumentChangeListener(id, context) {
                    work()
                }
            },
            change = {
                doc.setString("property", "changed value")
                saveDocInBaseTestDb(doc)
            },
            removeListener = { token ->
                baseTestDb.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testDocumentChangeCoroutineScopeListenerRemoved() {
        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initial value")
        saveDocInBaseTestDb(doc)

        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                baseTestDb.addDocumentChangeListener(id, scope) {
                    work()
                }
            },
            listenedChange = {
                doc.setString("property", "listened change")
                saveDocInBaseTestDb(doc)
            },
            notListenedChange = {
                doc.setString("property", "not listened change")
                saveDocInBaseTestDb(doc)
            }
        )
    }

    // QueryChange

    @Test
    fun testQueryChangeOnCoroutineContext() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))

        testOnCoroutineContext(
            addListener = { context, work ->
                query.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            }
        )
    }

    @Test
    fun testQueryChangeCoroutineCanceled() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))

        testCoroutineCanceled(
            addListener = { context, work ->
                query.addChangeListener(context) {
                    work()
                }
            },
            change = {
                saveDocInBaseTestDb(MutableDocument("newDoc"))
            },
            removeListener = { token ->
                query.removeChangeListener(token)
            }
        )
    }

    @Test
    fun testQueryChangeCoroutineScopeListenerRemoved() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(baseTestDb))

        testCoroutineScopeListenerRemoved(
            addListener = { scope, work ->
                query.addChangeListener(scope) {
                    work()
                }
            },
            listenedChange = {
                saveDocInBaseTestDb(MutableDocument("listenedDoc"))
            },
            notListenedChange = {
                saveDocInBaseTestDb(MutableDocument("notListenedDoc"))
            }
        )
    }
}
