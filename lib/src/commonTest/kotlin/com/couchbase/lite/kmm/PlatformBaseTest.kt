package com.couchbase.lite.kmm

import com.udobny.kmm.test.AndroidInstrumented

/**
 * Contains methods required for the tests to run on both Android and Java platforms.
 */
// annotate all test subclasses this way to ignore in androidTest unit tests
@AndroidInstrumented
expect abstract class PlatformBaseTest() {

    /* initialize the platform */
    fun setupPlatform()

    /* get a scratch directory */
    val tmpDir: String

    /* Reload the cross-platform error messages. */
    fun reloadStandardErrorMessages()

    /* Skip the test on some platforms */
    fun getExclusions(tag: String): Exclusion?

    //fun getExecutionService(executor: java.util.concurrent.ThreadPoolExecutor): AbstractExecutionService

    /* Schedule a task to be executed asynchronously. */
    fun executeAsync(delayMs: Long, task: () -> Unit)
}

class Exclusion(val msg: String, val test: () -> Boolean)
