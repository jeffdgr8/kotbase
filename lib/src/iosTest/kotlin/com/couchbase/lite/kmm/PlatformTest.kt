package com.couchbase.lite.kmm

import com.couchbase.lite.kmm.internal.useTestQueue
import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory

actual abstract class PlatformTest {

    actual fun setupPlatform() {
        useTestQueue = true
        val console = Database.log.console
        // iOS tests don't handle a lot of logging (may terminate prematurely on verbose log-level)
        console.level = LogLevel.WARNING
        console.domains = LogDomain.ALL_DOMAINS
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
