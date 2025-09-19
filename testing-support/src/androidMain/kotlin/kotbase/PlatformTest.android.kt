/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.couchbase.lite.internal.CouchbaseLiteInternal
import kotbase.internal.utils.FileUtils
import kotbase.logging.ConsoleLogSink
import kotbase.logging.LogSinks
import java.io.IOException

/**
 * Platform test class for Android.
 */
actual abstract class PlatformTest
actual constructor(private val useLegacyLogging: Boolean) {

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
            } catch (_: InterruptedException) {
                android.util.Log.w("TEST", "Failed adding to chatty whitelist")
            } catch (_: IOException) {
                android.util.Log.w("TEST", "Failed adding to chatty whitelist")
            }
        }
    }

    actual fun setupPlatform() {
        if (!useLegacyLogging) {
            LogSinks.console = ConsoleLogSink(LogLevel.INFO, LogDomain.ALL)
        } else {
            @Suppress("DEPRECATION")
            val console = Database.log.console
            console.level = LogLevel.INFO
            console.domains = LogDomain.ALL
        }
    }

    actual val tmpDir: String
        get() = FileUtils.verifyDir(
            appContext.getExternalFilesDir(
                SCRATCH_DIR_NAME
            )!!.absolutePath
        )

    actual val device: String?
        get() = android.os.Build.PRODUCT

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        val executionService = CouchbaseLiteInternal.getExecutionService()
        executionService.postDelayedOnExecutor(delayMs, executionService.defaultExecutor, task)
    }
}
