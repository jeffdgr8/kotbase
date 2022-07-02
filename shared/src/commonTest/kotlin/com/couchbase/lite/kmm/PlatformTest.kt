package com.couchbase.lite.kmm

/**
 * Contains methods required for the tests to run on both Android and Java platforms.
 */
interface PlatformTest {

    class Exclusion(val msg: String, val test: () -> Boolean)

    /* initialize the platform */
    fun setupPlatform()

    /* get a scratch directory */
    //val tmpDir: java.io.File?

    /* Reload the cross-platform error messages. */
    fun reloadStandardErrorMessages()

    /* Skip the test on some platforms */
    fun getExclusions(tag: String): Exclusion?

    //fun getExecutionService(executor: java.util.concurrent.ThreadPoolExecutor): AbstractExecutionService

    /* Schedule a task to be executed asynchronously. */
    fun executeAsync(delayMs: Long, task: () -> Unit)
}
