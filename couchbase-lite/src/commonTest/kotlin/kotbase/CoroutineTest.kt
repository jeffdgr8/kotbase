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

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val testContext = newSingleThreadContext("test-context-thread") + CoroutineName("test-context")

    private suspend fun checkContext(context: CoroutineContext) {
        assertEquals(context[CoroutineDispatcher], coroutineContext[CoroutineDispatcher])
        assertEquals(context[CoroutineName], coroutineContext[CoroutineName])
    }

    @Test
    fun testDatabaseChangeOnCoroutineContext() = runBlocking {
        val mutex = Mutex(true)

        baseTestDb.addChangeListener(testContext) { _ ->
            checkContext(testContext)
            mutex.unlock()
        }

        saveDocInBaseTestDb(MutableDocument("newDoc"))

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }
    }

    @Test
    fun testDatabaseChangeCoroutineCanceled() = runBlocking {
        val started = Mutex(true)
        val canceled = Mutex(true)

        val token = baseTestDb.addChangeListener(Dispatchers.Default) {
            try {
                started.unlock()
                delay(1000)
                fail("Coroutine should have been canceled")
            } catch (e: CancellationException) {
                canceled.unlock()
                throw e
            }
        }

        saveDocInBaseTestDb(MutableDocument("newDoc"))

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            started.lock()
        }

        baseTestDb.removeChangeListener(token)

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            canceled.lock()
        }
    }

    @Test
    fun testDatabaseChangeCoroutineScopeListenerRemoved() = runBlocking {
        val mutex = Mutex(true)

        val scope = CoroutineScope(SupervisorJob())
        val count = atomic(0)
        baseTestDb.addChangeListener(scope) {
            count.value++
            mutex.unlock()
        }

        saveDocInBaseTestDb(MutableDocument("withListener"))

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }

        scope.cancel()

        saveDocInBaseTestDb(MutableDocument("noListener"))

        delay(100) // give listener time to be called if still listening

        assertEquals(1, count.value)
    }

    @Test
    fun testDatabaseDocumentChangeOnCoroutineContext() = runBlocking {
        val mutex = Mutex(true)

        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initialValue")
        saveDocInBaseTestDb(doc)

        baseTestDb.addDocumentChangeListener(id, testContext) { _ ->
            checkContext(testContext)
            mutex.unlock()
        }

        doc.setString("property", "changedValue")
        saveDocInBaseTestDb(doc)

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }
    }

    @Test
    fun testDatabaseDocumentChangeCoroutineCanceled() = runBlocking {
        val started = Mutex(true)
        val canceled = Mutex(true)

        val id = "testDoc"
        val doc = MutableDocument(id)
        doc.setString("property", "initialValue")
        saveDocInBaseTestDb(doc)

        val token = baseTestDb.addDocumentChangeListener(id, Dispatchers.Default) {
            try {
                started.unlock()
                delay(1000)
                fail("Coroutine should have been canceled")
            } catch (e: CancellationException) {
                canceled.unlock()
                throw e
            }
        }

        doc.setString("property", "changedValue")
        saveDocInBaseTestDb(doc)

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            started.lock()
        }

        baseTestDb.removeChangeListener(token)

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            canceled.lock()
        }
    }
}
