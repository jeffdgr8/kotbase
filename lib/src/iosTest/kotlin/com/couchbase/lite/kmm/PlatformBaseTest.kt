package com.couchbase.lite.kmm

actual abstract class PlatformBaseTest {

    /* initialize the platform */
    actual fun setupPlatform() {
        // TODO:
    }

    /* Reload the cross-platform error messages. */
    actual fun reloadStandardErrorMessages() {
        // TODO:
    }

    /* Skip the test on some platforms */
    actual fun getExclusions(tag: String): Exclusion? {
        // TODO:
        return null
    }

    /* Schedule a task to be executed asynchronously. */
    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        // TODO:
    }
}
