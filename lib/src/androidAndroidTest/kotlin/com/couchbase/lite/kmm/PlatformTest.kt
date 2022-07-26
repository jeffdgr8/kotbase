package com.couchbase.lite.kmm

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.couchbase.lite.internal.CouchbaseLiteInternal
import com.couchbase.lite.kmm.internal.utils.FileUtils
import java.io.IOException

/**
 * Platform test class for Android.
 */
actual abstract class PlatformTest {

    companion object {

        const val SCRATCH_DIR_NAME = "cbl_test_scratch"

        private val appContext: Context
            get() = ApplicationProvider.getApplicationContext()

        init {
            CouchbaseLite.init(appContext, true)
        }

        init {
            try {
                Runtime.getRuntime()
                    .exec("logcat -P '" + android.os.Process.myPid() + "'").waitFor()
            } catch (e: InterruptedException) {
                android.util.Log.w("TEST", "Failed adding to chatty whitelist")
            } catch (e: IOException) {
                android.util.Log.w("TEST", "Failed adding to chatty whitelist")
            }
        }
    }

    actual fun setupPlatform() {
        val console = Database.log.console
        console.level = LogLevel.DEBUG
        console.domains = LogDomain.ALL_DOMAINS
    }

    actual val tmpDir: String
        get() = FileUtils.verifyDir(
            appContext.getExternalFilesDir(
                SCRATCH_DIR_NAME
            )!!.absolutePath
        )

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        val executionService = CouchbaseLiteInternal.getExecutionService()
        executionService.postDelayedOnExecutor(delayMs, executionService.defaultExecutor, task)
    }
}
