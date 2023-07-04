package kotbase

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

class CoroutineTest : BaseDbTest() {

    @Test
    fun testDatabaseChangeOnCoroutineContext() = runBlocking {
        val mutex = Mutex(true)

        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        val context = newSingleThreadContext("test-context-thread") + CoroutineName("test-context")
        baseTestDb.addChangeListener(context) { _ ->
            checkContext(context)
            mutex.unlock()
        }

        saveDocInBaseTestDb(MutableDocument("newDoc"))

        withTimeout(STD_TIMEOUT_SEC.seconds) {
            mutex.lock()
        }
    }

    private suspend fun checkContext(context: CoroutineContext) {
        assertEquals(context[CoroutineDispatcher], coroutineContext[CoroutineDispatcher])
        assertEquals(context[CoroutineName], coroutineContext[CoroutineName])
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
}
