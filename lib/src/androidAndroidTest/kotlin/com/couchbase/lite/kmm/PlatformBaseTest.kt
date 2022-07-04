package com.couchbase.lite.kmm

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.couchbase.lite.internal.AndroidExecutionService
import com.couchbase.lite.internal.CouchbaseLiteInternal
import com.couchbase.lite.internal.exec.AbstractExecutionService
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*
import java.util.concurrent.ThreadPoolExecutor

/**
 * Platform test class for Android.
 */
@RunWith(AndroidJUnit4::class)
actual abstract class PlatformBaseTest {

//    // appContext is unavailable in @BeforeClass
//    @Before
//    fun initCouchbaseLite() {
//        // ok that we call this multiple times, only runs internally once
//        CouchbaseLite.init(appContext, true)
//    }

    companion object {

        const val PRODUCT = "Android"
        const val SCRATCH_DIR_NAME = "cbl_test_scratch"
        const val LEGAL_FILE_NAME_CHARS = "`~@#$%^&()_+{}][=-.,;'12345ABCDEabcde"
        private val PLATFORM_DEPENDENT_TESTS: Map<String, Exclusion> = mapOf(
            "android<21" to Exclusion("Not supported on Android API < 21") { Build.VERSION.SDK_INT < 21 },
            "NOT WINDOWS" to Exclusion("Supported only on Windows") { true },
            "SWEDISH UNSUPPORTED" to Exclusion("Swedish locale not supported") {
                !Locale.getAvailableLocales().asList()
                    .contains(Locale("sv"))
            }
        )

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

    val tmpDir: java.io.File
        get() = com.couchbase.lite.internal.utils.FileUtils.verifyDir(
            appContext.getExternalFilesDir(
                SCRATCH_DIR_NAME
            )!!
        )

    actual fun reloadStandardErrorMessages() {
        com.couchbase.lite.internal.support.Log.initLogging(
            CouchbaseLiteInternal.loadErrorMessages(
                appContext
            )
        )
    }

    fun getExecutionService(executor: ThreadPoolExecutor): AbstractExecutionService {
        return AndroidExecutionService(executor)
    }

    actual fun executeAsync(delayMs: Long, task: () -> Unit) {
        val executionService = CouchbaseLiteInternal.getExecutionService()
        executionService.postDelayedOnExecutor(delayMs, executionService.defaultExecutor, task)
    }

    actual fun getExclusions(tag: String): Exclusion? {
        return PLATFORM_DEPENDENT_TESTS[tag]
    }
}
