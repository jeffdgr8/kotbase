@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase.internal.utils

import kotlin.random.Random

object StringUtils {

    const val ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val NUMERIC = "0123456789"
    val ALPHANUMERIC = NUMERIC + ALPHA + ALPHA.lowercase()
    private val CHARS = ALPHANUMERIC.toCharArray()

    fun getUniqueName(prefix: String, len: Int): String {
        return prefix + '_' + randomString(len)
    }

    fun randomString(len: Int): String {
        val buf = CharArray(len)
        for (idx in buf.indices) {
            buf[idx] = CHARS[Random.nextInt(CHARS.size)]
        }
        return buf.concatToString()
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

fun Int.paddedString(length: Int): String =
    toString().padStart(length, '0')

fun Long.paddedString(length: Int): String =
    toString().padStart(length, '0')
