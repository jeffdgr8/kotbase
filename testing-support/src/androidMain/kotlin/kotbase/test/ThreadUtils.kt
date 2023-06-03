@file:Suppress("unused")

package kotbase.test

import android.os.Looper

actual object ThreadUtils {

    actual fun logThread() {
        println("Thread = ${Thread.currentThread()}")
    }

    actual fun isMainThread(): Boolean =
        Looper.getMainLooper() == Looper.myLooper()

    actual fun logStackTrace() {
        println("Current stack trace:")
        Throwable().printStackTrace()
    }
}
