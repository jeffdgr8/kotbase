package com.couchbase.lite.kmm

import com.couchbase.lite.kmm.PlatformTest.Exclusion

actual abstract class PlatformBaseTest : PlatformTest {

    /* initialize the platform */
    override fun setupPlatform() {
        // TODO:
    }

    /* get a scratch directory */
    //val tmpDir: java.io.File?

    /* Reload the cross-platform error messages. */
    override fun reloadStandardErrorMessages() {
        // TODO:
    }

    /* Skip the test on some platforms */
    override fun getExclusions(tag: String): Exclusion? {
        // TODO:
        return null
    }

    //fun getExecutionService(executor: java.util.concurrent.ThreadPoolExecutor?): AbstractExecutionService?

    /* Schedule a task to be executed asynchronously. */
    override fun executeAsync(delayMs: Long, task: () -> Unit) {
        // TODO:
    }
}
