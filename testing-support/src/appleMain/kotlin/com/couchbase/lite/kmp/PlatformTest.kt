package com.couchbase.lite.kmp

import com.couchbase.lite.kmp.internal.utils.FileUtils
import kotlinx.cinterop.convert
import platform.Foundation.NSFileManager
import platform.Foundation.temporaryDirectory
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_time
import platform.posix.QOS_CLASS_DEFAULT

actual abstract class PlatformTest {

    companion object {
        const val SCRATCH_DIR_NAME = "/cbl_test_scratch"
    }

    actual fun setupPlatform() {
        val console = Database.log.console
        // iOS tests don't handle a lot of logging (may terminate prematurely on verbose log-level)
        console.level = LogLevel.WARNING
        console.domains = LogDomain.ALL_DOMAINS
    }

    actual val tmpDir: String
        get() {
            return FileUtils.verifyDir(
                NSFileManager.defaultManager.temporaryDirectory.path!! + SCRATCH_DIR_NAME
            )
        }

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, delayMs),
            dispatch_get_global_queue(QOS_CLASS_DEFAULT.convert(), 0.convert()),
            task
        )
    }
}