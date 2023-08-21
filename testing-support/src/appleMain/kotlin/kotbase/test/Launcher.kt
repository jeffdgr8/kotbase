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
