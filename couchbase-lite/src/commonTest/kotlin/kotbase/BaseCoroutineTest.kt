package kotbase

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

abstract class BaseCoroutineTest : BaseReplicatorTest() {

    protected fun testOnCoroutineContext(
        addListener: (context: CoroutineContext, work: suspend () -> Unit) -> Unit,
        change: () -> Unit
    ) = runBlocking {
        val mutex = Mutex(true)
        @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
        val testContext = newSingleThreadContext("test-context-thread") + CoroutineName("test-context")
        addListener(testContext) {
            checkContext(testContext)
            println("context check passed ${mutex.isLocked}")
            if (mutex.isLocked) mutex.unlock()
        }
        change()
        withTimeout(STD_TIMEOUT_SEC.seconds) {
            println("waiting on mutex")
            mutex.lock()
        }
        println("done!")
    }

    private suspend fun checkContext(context: CoroutineContext) {
        assertEquals(context[CoroutineDispatcher], coroutineContext[CoroutineDispatcher])
        assertEquals(context[CoroutineName], coroutineContext[CoroutineName])
    }

    protected fun testCoroutineCanceled(
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

    protected fun testCoroutineScopeListenerRemoved(
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
}