package com.couchbase.lite.kmp

import com.couchbase.lite.internal.CouchbaseLiteInternal
import com.couchbase.lite.kmp.internal.utils.FileUtils
import java.io.File

/**
 * Platform test class for Android.
 */
actual abstract class PlatformTest {

    companion object {

        const val SCRATCH_DIR_NAME = "cbl_test_scratch"

        init {
            val rootDir = File("build/cb-tmp")
            CouchbaseLite.init(true, rootDir, rootDir)
        }
    }

    actual fun setupPlatform() {
        val console = Database.log.console
        console.level = LogLevel.DEBUG
        console.domains = LogDomain.ALL_DOMAINS
    }

    actual val tmpDir: String
        get() = FileUtils.verifyDir(File("build${FileUtils.separatorChar}cb-tmp${FileUtils.separatorChar}$SCRATCH_DIR_NAME"))

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        val executionService = CouchbaseLiteInternal.getExecutionService()
        executionService.postDelayedOnExecutor(delayMs, executionService.defaultExecutor, task)
    }
}
