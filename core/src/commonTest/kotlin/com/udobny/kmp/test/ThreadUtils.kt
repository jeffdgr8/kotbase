package com.udobny.kmp.test

expect object ThreadUtils {

    fun logThread()

    fun isMainThread(): Boolean

    fun logStackTrace()
}
