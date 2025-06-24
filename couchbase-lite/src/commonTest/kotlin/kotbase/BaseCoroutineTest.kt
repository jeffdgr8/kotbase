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
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
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
            if (mutex.isLocked) mutex.unlock()
        }
        change()
        assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
    }

    private suspend fun checkContext(context: CoroutineContext) {
        assertEquals(context[CoroutineDispatcher], coroutineContext[CoroutineDispatcher])
        assertEquals(context[CoroutineName], coroutineContext[CoroutineName])
    }

    protected fun testCoroutineCanceled(
        addListener: (context: CoroutineContext, work: suspend () -> Unit) -> ListenerToken,
        change: () -> Unit
    ) = runBlocking {
        val started = Mutex(true)
        val canceled = Mutex(true)
        val token = addListener(Dispatchers.Default.limitedParallelism(1)) {
            try {
                if (!started.isLocked) return@addListener
                started.unlock()
                delay(STD_TIMEOUT_SEC.seconds)
                fail("Coroutine should have been canceled")
            } catch (e: CancellationException) {
                if (canceled.isLocked) canceled.unlock()
                throw e
            }
        }
        change()
        assertTrue(started.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        token.remove()
        assertTrue(canceled.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
    }

    @OptIn(ExperimentalAtomicApi::class)
    protected fun testCoroutineScopeListenerRemoved(
        addListener: (scope: CoroutineScope, work: () -> Unit) -> Unit,
        listenedChange: () -> Unit,
        notListenedChange: () -> Unit
    ) = runBlocking {
        val mutex = Mutex(true)
        val scope = CoroutineScope(SupervisorJob())
        val canceled = AtomicBoolean(false)
        addListener(scope) {
            assertFalse(canceled.load())
            if (mutex.isLocked) mutex.unlock()
        }
        listenedChange()
        assertTrue(mutex.lockWithTimeout(STD_TIMEOUT_SEC.seconds))
        scope.cancel()
        canceled.store(true)
        notListenedChange()
        delay(200) // give listener time to be called if still listening
    }
}
