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
package kotbase.test

import platform.CoreFoundation.CFRunLoopRun
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.concurrent.ObsoleteWorkersApi
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.internal.test.testLauncherEntryPoint
import kotlin.system.exitProcess

/**
 * Background test runner with main run loop
 */
@OptIn(ExperimentalNativeApi::class, ObsoleteWorkersApi::class)
@Suppress("unused")
fun mainBackground(args: Array<String>) {
    val worker = Worker.start(name = "main-background")
    worker.execute(TransferMode.SAFE, { args }) {
        val result = testLauncherEntryPoint(it)
        exitProcess(result)
    }
    CFRunLoopRun()
    error("CFRunLoopRun should never return")
}
