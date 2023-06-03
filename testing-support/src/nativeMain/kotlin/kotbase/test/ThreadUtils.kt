package kotbase.test

import platform.posix.getpid
import platform.posix.pthread_self

// TODO:
actual object ThreadUtils {

    actual fun logThread() {
        println("Thread = ${pthread_self()}")
    }

    actual fun isMainThread(): Boolean =
        getpid().toULong() == pthread_self()

    actual fun logStackTrace() {
        println("Current stack trace:")
        println("(TODO)")
    }
}
