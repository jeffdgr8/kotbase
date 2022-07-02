package com.couchbase.lite.kmm.internal.utils

import kotlin.random.Random

object StringUtils {

    const val ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val NUMERIC = "0123456789"
    val ALPHANUMERIC = NUMERIC + ALPHA + ALPHA.lowercase()
    private val CHARS: CharArray = ALPHANUMERIC.toCharArray()

    fun getUniqueName(prefix: String, len: Int): String {
        return prefix + '_' + randomString(len)
    }

    fun randomString(len: Int): String {
        val buf = CharArray(len)
        for (idx in buf.indices) {
            buf[idx] = CHARS.get(
                Random.nextInt(CHARS.size)
            )
        }
        return buf.concatToString()
    }

    fun getArrayString(strs: Array<String?>?, idx: Int): String {
        return if (strs == null || idx < 0 || idx >= strs.size) "" else strs[idx]!!
    }

    fun toString(map: Map<*, *>?): String {
        val buf = StringBuilder()
        if (map != null) {
            var i = 0
            for ((key, value) in map) {
                if (i++ > 0) {
                    buf.append(", ")
                }
                buf.append(key).append("=>").append(value)
            }
        }
        return buf.toString()
    }
}
