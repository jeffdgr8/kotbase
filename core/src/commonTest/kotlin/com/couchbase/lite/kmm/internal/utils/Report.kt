package com.couchbase.lite.kmm.internal.utils

import com.couchbase.lite.kmm.LogLevel
import dev.simplx.KotlinFormatter

object Report {

    private const val DOMAIN = "CouchbaseLite/TEST"

    fun log(message: String) {
        log(LogLevel.INFO, message, null as Throwable?)
    }

    fun log(err: Throwable?, message: String) {
        log(LogLevel.INFO, message, err)
    }

    fun log(template: String, vararg args: Any?) {
        log(LogLevel.INFO, KotlinFormatter.format(template, *args))
    }

    fun log(err: Throwable?, template: String, vararg args: Any?) {
        log(LogLevel.INFO, KotlinFormatter.format(template, *args), err)
    }

    fun log(level: LogLevel, message: String) {
        log(level, message, null as Throwable?)
    }

    fun log(level: LogLevel, template: String, vararg args: Any?) {
        log(level, KotlinFormatter.format(template, *args))
    }

    fun log(level: LogLevel, err: Throwable?, template: String, vararg args: Any?) {
        log(level, KotlinFormatter.format(template, *args), err)
    }

    fun log(level: LogLevel, message: String, err: Throwable?) {
        println("$level $DOMAIN $message")
        err?.printStackTrace()
    }
}
