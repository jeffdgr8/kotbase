@file:Suppress("unused")

package kotbase.test

import platform.Foundation.NSThread

actual object ThreadUtils {

    actual fun logThread() {
        println("Thread = ${NSThread.currentThread}")
    }

    actual fun isMainThread(): Boolean =
        NSThread.isMainThread

    actual fun logStackTrace() {
        println("Current stack trace:")
        NSThread.callStackSymbols.forEach {
            println(it)
        }
    }
}
