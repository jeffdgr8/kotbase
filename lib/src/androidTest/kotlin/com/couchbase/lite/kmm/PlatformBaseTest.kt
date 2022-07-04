package com.couchbase.lite.kmm

/**
 * Platform test class for Android.
 */
actual abstract class PlatformBaseTest {

    actual fun setupPlatform() {
    }

    actual fun reloadStandardErrorMessages() {
    }

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
    }

    actual fun getExclusions(tag: String): Exclusion? {
        return null
    }
}
