package kotbase.internal.utils

import kotbase.LogLevel

object Report {

    private const val DOMAIN = "CouchbaseLite/TEST"

    fun log(message: String) {
        log(message, null)
    }

    fun log(message: String, err: Throwable?) {
        log(LogLevel.INFO, message, err)
    }

    fun log(level: LogLevel, message: String, err: Throwable?) {
        println("$level $DOMAIN $message")
        err?.printStackTrace()
    }
}
