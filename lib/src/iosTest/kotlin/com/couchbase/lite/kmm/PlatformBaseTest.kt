package com.couchbase.lite.kmm

import com.couchbase.lite.kmm.internal.useTestQueue
import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory

actual abstract class PlatformBaseTest {

    actual fun setupPlatform() {
        useTestQueue = true
        // TODO:
    }

    actual val tmpDir: String
        get() = NSFileManager.defaultManager.temporaryDirectory.absoluteString!!

    actual fun reloadStandardErrorMessages() {
        // TODO:
    }

    actual fun getExclusions(tag: String): Exclusion? {
        // TODO:
        return null
    }

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        // TODO:
    }
}