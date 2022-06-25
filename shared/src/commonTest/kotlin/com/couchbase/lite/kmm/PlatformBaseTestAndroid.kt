//package com.couchbase.lite.kmm
//
////
//// Copyright (c) 2020 Couchbase, Inc.
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
////
//import androidx.test.core.app.ApplicationProvider
//
//
///**
// * Platform test class for Android.
// */
//abstract class PlatformBaseTestAndroid : PlatformTest {
//    companion object {
//        const val PRODUCT = "Android"
//        const val SCRATCH_DIR_NAME = "cbl_test_scratch"
//        const val LEGAL_FILE_NAME_CHARS = "`~@#$%^&()_+{}][=-.,;'12345ABCDEabcde"
//        private val PLATFORM_DEPENDENT_TESTS: Map<String, Exclusion>? = null
//        private val appContext: android.content.Context
//            private get() = ApplicationProvider.getApplicationContext()
//
//        init {
//            val m: MutableMap<String, Exclusion> = java.util.HashMap<String, Exclusion>()
//            m["android<21"] =
//                Exclusion("Not supported on Android API < 21") { Build.VERSION.SDK_INT < 21 }
//            m["NOT WINDOWS"] = Exclusion("Supported only on Windows") { true }
//            m["SWEDISH UNSUPPORTED"] = Exclusion(
//                "Swedish locale not supported"
//            ) {
//                !java.util.Arrays.asList<java.util.Locale>(*java.util.Locale.getAvailableLocales())
//                    .contains(java.util.Locale("sv"))
//            }
//            PLATFORM_DEPENDENT_TESTS = java.util.Collections.unmodifiableMap(m)
//        }
//
//        init {
//            CouchbaseLite.init(Companion.getAppContext(), true)
//        }
//
//        init {
//            try {
//                java.lang.Runtime.getRuntime()
//                    .exec("logcat -P '" + android.os.Process.myPid() + "'").waitFor()
//            } catch (e: java.lang.InterruptedException) {
//                android.util.Log.w("TEST", "Failed adding to chatty whitelist")
//            } catch (e: java.io.IOException) {
//                android.util.Log.w("TEST", "Failed adding to chatty whitelist")
//            }
//        }
//    }
//
//    fun setupPlatform() {
//        val console: ConsoleLogger = Database.log.getConsole()
//        console.level = LogLevel.DEBUG
//        console.domains = LogDomain.ALL_DOMAINS
//    }
//
//    val tmpDir: java.io.File
//        get() = com.couchbase.lite.internal.utils.FileUtils.verifyDir(
//            Companion.getAppContext().getExternalFilesDir(
//                SCRATCH_DIR_NAME
//            )
//        )
//
//    fun reloadStandardErrorMessages() {
//        com.couchbase.lite.internal.support.Log.initLogging(
//            CouchbaseLiteInternal.loadErrorMessages(
//                Companion.getAppContext()
//            )
//        )
//    }
//
//    fun getExecutionService(executor: java.util.concurrent.ThreadPoolExecutor?): AbstractExecutionService {
//        return AndroidExecutionService(executor)
//    }
//
//    fun executeAsync(delayMs: Long, task: java.lang.Runnable?) {
//        val executionService: ExecutionService = CouchbaseLiteInternal.getExecutionService()
//        executionService.postDelayedOnExecutor(delayMs, executionService.getDefaultExecutor(), task)
//    }
//
//    fun getExclusions(@NonNull tag: String?): Exclusion? {
//        return PLATFORM_DEPENDENT_TESTS!![tag!!]
//    }
//}