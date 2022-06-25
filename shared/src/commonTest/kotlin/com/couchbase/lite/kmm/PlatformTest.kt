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
//
//
///**
// * Contains methods required for the tests to run on both Android and Java platforms.
// */
//interface PlatformTest {
//    class Exclusion internal constructor(
//        @param:NonNull val msg: String,
//        test: com.couchbase.lite.internal.utils.Fn.Provider<Boolean?>
//    ) {
//        val test: com.couchbase.lite.internal.utils.Fn.Provider<Boolean>
//
//        init {
//            this.test = test
//        }
//    }
//
//    /* initialize the platform */
//    fun setupPlatform()
//
//    /* get a scratch directory */
//    val tmpDir: java.io.File?
//
//    /* Reload the cross-platform error messages. */
//    fun reloadStandardErrorMessages()
//
//    /* Skip the test on some platforms */
//    fun getExclusions(@NonNull tag: String?): Exclusion?
//    fun getExecutionService(executor: java.util.concurrent.ThreadPoolExecutor?): AbstractExecutionService?
//
//    /* Schedule a task to be executed asynchronously. */
//    fun executeAsync(delayMs: Long, task: java.lang.Runnable?)
//}