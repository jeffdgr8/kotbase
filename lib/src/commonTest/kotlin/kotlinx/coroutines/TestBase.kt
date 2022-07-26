@file:Suppress("unused", "RedundantUnitReturnType", "RedundantVisibilityModifier", "Reformat",
    "MayBeConstant"
)

// adapted from https://github.com/Kotlin/kotlinx.coroutines/blob/042720589c6f438f77d84254bd2dceb569f01841/kotlinx-coroutines-core/common/test/TestBase.common.kt
// and native https://github.com/Kotlin/kotlinx.coroutines/blob/042720589c6f438f77d84254bd2dceb569f01841/kotlinx-coroutines-core/native/test/TestBase.kt
/*
 * Copyright 2016-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.coroutines

import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.test.*

import kotlinx.atomicfu.*

public val isStressTest: Boolean = false
public val stressTestMultiplier: Int = 1
public val stressTestMultiplierSqrt: Int = 1

public val isNative = true

/**
 * The result of a multiplatform asynchronous test.
 * Aliases into Unit on K/JVM and K/N, and into Promise on K/JS.
 */
@Suppress("ACTUAL_WITHOUT_EXPECT")
public typealias TestResult = Unit

public open class TestBase {
    /*
     * In common tests we emulate parameterized tests
     * by iterating over parameters space in the single @Test method.
     * This kind of tests is too slow for JS and does not fit into
     * the default Mocha timeout, so we're using this flag to bail-out
     * and run such tests only on JVM and K/N.
     */
    public val isBoundByJsTestTimeout = false
    private var actionIndex = atomic(0)
    private var finished = atomic(false)
    private var error: Throwable? = null

    /**
     * Throws [IllegalStateException] like `error` in stdlib, but also ensures that the test will not
     * complete successfully even if this exception is consumed somewhere in the test.
     */
    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public fun error(message: Any, cause: Throwable? = null): Nothing {
        val exception = IllegalStateException(message.toString(), cause)
        if (error == null) error = exception
        throw exception
    }

    private fun printError(message: String, cause: Throwable) {
        if (error == null) error = cause
        println("$message: $cause")
    }

    /**
     * Asserts that this invocation is `index`-th in the execution sequence (counting from one).
     */
    public fun expect(index: Int) {
        val wasIndex = actionIndex.incrementAndGet()
        check(index == wasIndex) { "Expecting action index $index but it is actually $wasIndex" }
    }

    /**
     * Asserts that this line is never executed.
     */
    public fun expectUnreached() {
        error("Should not be reached")
    }

    /**
     * Asserts that this it the last action in the test. It must be invoked by any test that used [expect].
     */
    public fun finish(index: Int) {
        expect(index)
        check(!finished.value) { "Should call 'finish(...)' at most once" }
        finished.value = true
    }

    /**
     * Asserts that [finish] was invoked
     */
    fun ensureFinished() {
        require(finished.value) { "finish(...) should be caller prior to this check" }
    }

    fun reset() {
        check(actionIndex.value == 0 || finished.value) { "Expecting that 'finish(...)' was invoked, but it was not" }
        actionIndex.value = 0
        finished.value = false
    }

    @Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
    public fun runTest(
        expected: ((Throwable) -> Boolean)? = null,
        unhandled: List<(Throwable) -> Boolean> = emptyList(),
        block: suspend CoroutineScope.() -> Unit
    ): TestResult {
        var exCount = 0
        var ex: Throwable? = null
        try {
            runBlocking(block = block, context = CoroutineExceptionHandler { _, e ->
                if (e is CancellationException) return@CoroutineExceptionHandler // are ignored
                exCount++
                when {
                    exCount > unhandled.size ->
                        printError("Too many unhandled exceptions $exCount, expected ${unhandled.size}, got: $e", e)
                    !unhandled[exCount - 1](e) ->
                        printError("Unhandled exception was unexpected: $e", e)
                }
            })
        } catch (e: Throwable) {
            ex = e
            if (expected != null) {
                if (!expected(e))
                    error("Unexpected exception: $e", e)
            } else
                throw e
        } finally {
            if (ex == null && expected != null) error("Exception was expected but none produced")
        }
        if (exCount < unhandled.size)
            error("Too few unhandled exceptions $exCount, expected ${unhandled.size}")
    }
}

public suspend inline fun hang(onCancellation: () -> Unit) {
    try {
        suspendCancellableCoroutine<Unit> { }
    } finally {
        onCancellation()
    }
}

public inline fun <reified T : Throwable> assertFailsWith(block: () -> Unit) {
    try {
        block()
        error("Should not be reached")
    } catch (e: Throwable) {
        assertTrue(e is T)
    }
}

public suspend inline fun <reified T : Throwable> assertFailsWith(flow: Flow<*>) {
    try {
        flow.collect()
        fail("Should be unreached")
    } catch (e: Throwable) {
        assertTrue(e is T, "Expected exception ${T::class}, but had $e instead")
    }
}

public suspend fun Flow<Int>.sum() = fold(0) { acc, value -> acc + value }
public suspend fun Flow<Long>.longSum() = fold(0L) { acc, value -> acc + value }


// data is added to avoid stacktrace recovery because CopyableThrowable is not accessible from common modules
public class TestException(message: String? = null, private val data: Any? = null) : Throwable(message)
public class TestException1(message: String? = null, private val data: Any? = null) : Throwable(message)
public class TestException2(message: String? = null, private val data: Any? = null) : Throwable(message)
public class TestException3(message: String? = null, private val data: Any? = null) : Throwable(message)
public class TestCancellationException(message: String? = null, private val data: Any? = null) : CancellationException(message)
public class TestRuntimeException(message: String? = null, private val data: Any? = null) : RuntimeException(message)
public class RecoverableTestException(message: String? = null) : RuntimeException(message)
public class RecoverableTestCancellationException(message: String? = null) : CancellationException(message)

public fun wrapperDispatcher(context: CoroutineContext): CoroutineContext {
    val dispatcher = context[ContinuationInterceptor] as CoroutineDispatcher
    return object : CoroutineDispatcher() {
        override fun isDispatchNeeded(context: CoroutineContext): Boolean =
            dispatcher.isDispatchNeeded(context)
        override fun dispatch(context: CoroutineContext, block: Runnable) =
            dispatcher.dispatch(context, block)
    }
}

public suspend fun wrapperDispatcher(): CoroutineContext = wrapperDispatcher(coroutineContext)

class BadClass {
    override fun equals(other: Any?): Boolean = error("equals")
    override fun hashCode(): Int = error("hashCode")
    override fun toString(): String = error("toString")
}
