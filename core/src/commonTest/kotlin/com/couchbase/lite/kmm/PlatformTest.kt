package com.couchbase.lite.kmm

/**
 * Contains methods required for the tests to run on both Android and Java platforms.
 */
expect abstract class PlatformTest() {

    /* initialize the platform */
    fun setupPlatform()

    /* get a scratch directory */
    val tmpDir: String

    /* Schedule a task to be executed asynchronously. */
    fun executeAsync(delayMs: Long, task: () -> Unit)
}
