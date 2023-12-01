/*
 * Copyright 2023 Jeff Lockhart
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

/*
 * Adapted from https://github.com/arrow-kt/arrow/blob/main/arrow-libs/fx/arrow-fx-coroutines/src/commonMain/kotlin/arrow/fx/coroutines/CyclicBarrier.kt
 */

package kotlinx.coroutines.sync

import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlinx.atomicfu.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

/**
 * A [CyclicBarrier] is a synchronization mechanism that allows a set of coroutines to wait for each other
 * to reach a certain point before continuing execution.
 * It is called a "cyclic" barrier because it can be reused after all coroutines have reached the barrier and released.
 *
 * To use a CyclicBarrier, each coroutine must call the [await] method on the barrier object,
 * which will cause the coroutine to suspend until the required number of coroutines have reached the barrier.
 * Once all coroutines have reached the barrier they will _resume_ execution.
 *
 * Models the behavior of java.util.concurrent.CyclicBarrier in Kotlin with `suspend`.
 *
 * @param capacity The number of coroutines that must await until the barrier cycles and all are released.
 * @param barrierAction An optional runnable that will be executed when the barrier is cycled, but before releasing.
 */
public class CyclicBarrier(public val capacity: Int, private val barrierAction: () -> Unit = {}) {
    init {
        require(capacity > 0) {
            "Cyclic barrier must be constructed with positive non-zero capacity $capacity but was $capacity > 0"
        }
    }

    private sealed interface State {
        val epoch: Long
    }

    private data class Awaiting(
        /** Current number of waiting parties. **/
        val awaitingNow: Int,
        override val epoch: Long,
        val unblock: CompletableDeferred<Unit>
    ) : State

    private data class Resetting(
        val awaitingNow: Int,
        override val epoch: Long,
        /** Barrier used to ensure all awaiting threads are ready to reset. **/
        val unblock: CompletableDeferred<Unit>
    ) : State

    private val state: AtomicRef<State> = atomic(Awaiting(capacity, 0, CompletableDeferred()))

    /**
     * When called, all waiting coroutines will be cancelled with [CancellationException].
     * When all coroutines have been cancelled the barrier will cycle.
     */
    public suspend fun reset() {
        when (val original = state.value) {
            is Awaiting -> {
                val resetBarrier = CompletableDeferred<Unit>()
                if (state.compareAndSet(original, Resetting(original.awaitingNow, original.epoch, resetBarrier))) {
                    original.unblock.cancel(CyclicBarrierCancellationException())
                    resetBarrier.await()
                } else reset()
            }

            // We're already resetting, await all waiters to finish
            is Resetting -> original.unblock.await()
        }
    }

    private fun attemptBarrierAction(unblock: CompletableDeferred<Unit>) {
        try {
            barrierAction.invoke()
        } catch (e: Throwable) {
            val cancellationException =
                if (e is CancellationException) e
                else CancellationException("CyclicBarrier barrierAction failed with exception.", e)
            unblock.cancel(cancellationException)
            throw cancellationException
        }
    }

    /**
     * When [await] is called the function will suspend until the required number of coroutines have called [await].
     * Once the [capacity] of the barrier has been reached, the coroutine will be released and continue execution.
     *
     * If the specified waiting time elapses then the value `false`
     * is returned.  If the time is less than or equal to zero, the method
     * will not wait at all.
     *
     * @param timeout the maximum time to wait
     * @return `true` if the [capacity] of the barrier was reached and `false`
     *         if the waiting time elapsed before the capacity was reached
     * @throws CancellationException if the current coroutine is cancelled
     *         while waiting or is already cancelled
     */
    suspend fun await(timeout: Duration): Boolean =
        withTimeoutOrNull(timeout) { await() } != null

    /**
     * When [await] is called the function will suspend until the required number of coroutines have called [await].
     * Once the [capacity] of the barrier has been reached, the coroutine will be released and continue execution.
     */
    public suspend fun await() {
        state.loop { state ->
            when (state) {
                is Awaiting -> {
                    val (awaiting, epoch, unblock) = state
                    val awaitingNow = awaiting - 1
                    if (awaitingNow == 0 && this.state.compareAndSet(
                            state,
                            Awaiting(capacity, epoch + 1, CompletableDeferred())
                        )
                    ) {
                        attemptBarrierAction(unblock)
                        unblock.complete(Unit)
                        return
                    } else if (this.state.compareAndSet(state, Awaiting(awaitingNow, epoch, unblock))) {
                        return try {
                            unblock.await()
                        } catch (c: CyclicBarrierCancellationException) {
                            countdown(state, c)
                            throw c
                        } catch (cancelled: CancellationException) {
                            this.state.update { s ->
                                when {
                                    s is Awaiting && s.epoch == epoch -> s.copy(awaitingNow = s.awaitingNow + 1)
                                    else -> s
                                }
                            }
                            throw cancelled

                        }
                    }
                }

                is Resetting -> {
                    state.unblock.await()
                    // State resets to `Awaiting` after `reset.unblock`.
                    // Unless there is another racing reset, it will be in `Awaiting` in next loop.
                    await()
                }
            }
        }
    }

    private fun countdown(original: Awaiting, ex: CyclicBarrierCancellationException): Boolean {
        state.loop { state ->
            when (state) {
                is Resetting -> {
                    val awaitingNow = state.awaitingNow + 1
                    if (awaitingNow < capacity && this.state.compareAndSet(state, state.copy(awaitingNow = awaitingNow))) {
                        return false
                    } else if (awaitingNow == capacity && this.state.compareAndSet(
                            state, Awaiting(capacity, state.epoch + 1, CompletableDeferred())
                        )
                    ) {
                        return state.unblock.complete(Unit)
                    } else countdown(original, ex)
                }

                is Awaiting -> throw IllegalStateException("Awaiting appeared during resetting.")
            }
        }
    }

}

public class CyclicBarrierCancellationException : CancellationException("CyclicBarrier was cancelled")
