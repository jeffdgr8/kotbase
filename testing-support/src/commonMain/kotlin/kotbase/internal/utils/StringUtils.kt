/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package kotbase.internal.utils

import kotlin.random.Random

object StringUtils {

    const val ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    const val NUMERIC = "0123456789"
    val ALPHANUMERIC = NUMERIC + ALPHA + ALPHA.lowercase()
    private val CHARS = ALPHANUMERIC.toCharArray()

    fun getUniqueName(prefix: String, len: Int): String = prefix + '_' + randomString(len)

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
