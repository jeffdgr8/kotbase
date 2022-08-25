package com.udobny.kmp.test

import java.awt.EventQueue

actual object ThreadUtils {

    actual fun logThread() {
        println("Thread = ${Thread.currentThread()}")
    }

    actual fun isMainThread(): Boolean =
        EventQueue.isDispatchThread()

    actual fun logStackTrace() {
        println("Current stack trace:")
        Throwable().printStackTrace()
    }
}
