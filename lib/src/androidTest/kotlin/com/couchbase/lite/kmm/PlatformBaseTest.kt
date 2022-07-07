package com.couchbase.lite.kmm

/**
 * Dummy class (unused in unit tests)
 */
actual abstract class PlatformBaseTest {

    actual fun setupPlatform() {}

    actual val tmpDir: String = ""

    actual fun reloadStandardErrorMessages() {}

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {}

    actual fun getExclusions(tag: String): Exclusion? = null
}
