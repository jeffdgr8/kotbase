@file:Suppress("unused")

package com.udobny.kmm.test

import platform.CoreFoundation.CFRunLoopRun
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.native.concurrent.freeze
import kotlin.native.internal.test.testLauncherEntryPoint
import kotlin.system.exitProcess

/**
 * Background test runner with main run loop
 */
fun mainBackground(args: Array<String>) {
    val worker = Worker.start(name = "main-background")
    worker.execute(TransferMode.SAFE, { args.freeze() }) {
        val result = testLauncherEntryPoint(it)
        exitProcess(result)
    }
    CFRunLoopRun()
    error("CFRunLoopRun should never return")
}
