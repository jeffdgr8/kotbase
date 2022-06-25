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
//import com.couchbase.lite.internal.JavaExecutionService
//
//
///**
// * Platform test class for Java.
// */
//abstract class PlatformBaseTest : PlatformTest {
//    companion object {
//        const val PRODUCT = "Java"
//        const val SCRATCH_DIR_NAME = "cbl_test_scratch"
//        const val LEGAL_FILE_NAME_CHARS = "`~@#$%&'()_+{}][=-.,;'ABCDEabcde"
//        const val LOG_DIR = "logs"
//        private const val MAX_LOG_FILE_BYTES = Long.MAX_VALUE // lots
//        private const val MAX_LOG_FILES = Int.MAX_VALUE // lots
//        private val PLATFORM_DEPENDENT_TESTS: Map<String, Exclusion>? = null
//        private var logConfig: LogFileConfiguration? = null
//
//        init {
//            val m: MutableMap<String, Exclusion> = java.util.HashMap<String, Exclusion>()
//            m["NOT WINDOWS"] = Exclusion(
//                "Supported only on Windows"
//            ) {
//                !java.lang.System.getProperty("os.name").lowercase(java.util.Locale.getDefault())
//                    .contains("windows")
//            }
//            m["WINDOWS"] = Exclusion(
//                "Not supported on Windows"
//            ) {
//                java.lang.System.getProperty("os.name").lowercase(java.util.Locale.getDefault())
//                    .contains("windows")
//            }
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
//            CouchbaseLite.init(true)
//        }
//    }
//
//    // set up the file logger...
//    fun setupPlatform() {
//        if (logConfig == null) {
//            val logDirPath: String
//            logDirPath = try {
//                com.couchbase.lite.internal.utils.FileUtils.verifyDir(
//                    java.io.File(
//                        java.io.File("").getCanonicalFile(), LOG_DIR
//                    )
//                ).getCanonicalPath()
//            } catch (e: java.io.IOException) {
//                throw java.lang.IllegalStateException("Could not find log directory", e)
//            }
//            logConfig = LogFileConfiguration(logDirPath)
//                .setUsePlaintext(true)
//                .setMaxSize(MAX_LOG_FILE_BYTES)
//                .setMaxRotateCount(MAX_LOG_FILES)
//        }
//        val logger: com.couchbase.lite.Log = Database.log
//        val fileLogger: FileLogger = logger.getFile()
//        if (!logConfig!!.equals(fileLogger.config)) {
//            fileLogger.config = logConfig
//        }
//        fileLogger.level = LogLevel.DEBUG
//        val consoleLogger: ConsoleLogger = logger.getConsole()
//        consoleLogger.level = LogLevel.DEBUG
//        consoleLogger.domains = LogDomain.ALL_DOMAINS
//    }
//
//    val tmpDir: java.io.File
//        get() = com.couchbase.lite.internal.utils.FileUtils.verifyDir(
//            java.io.File(
//                com.couchbase.lite.internal.utils.FileUtils.getCurrentDirectory(),
//                SCRATCH_DIR_NAME
//            )
//        )
//
//    fun reloadStandardErrorMessages() {
//        com.couchbase.lite.internal.support.Log.initLogging(CouchbaseLiteInternal.loadErrorMessages())
//    }
//
//    fun getExclusions(@NonNull tag: String): Exclusion? {
//        return PLATFORM_DEPENDENT_TESTS!![tag]
//    }
//
//    fun getExecutionService(executor: java.util.concurrent.ThreadPoolExecutor?): AbstractExecutionService {
//        return JavaExecutionService(executor)
//    }
//
//    fun executeAsync(delayMs: Long, task: java.lang.Runnable?) {
//        val executionService: ExecutionService = CouchbaseLiteInternal.getExecutionService()
//        executionService.postDelayedOnExecutor(delayMs, executionService.getDefaultExecutor(), task)
//    }
//}