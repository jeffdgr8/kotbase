@file:Suppress("unused")

package dev.simplx

/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
class FormatToken {
    var formatStringStartIndex = 0
    var plainText: String? = null
    var argIndex = UNSET
    var flags = 0
    var width = UNSET
    var precision = UNSET
    private val strFlags = StringBuilder(FLAGT_TYPE_COUNT)
    var dateSuffix // will be used in new feature.
            = 0.toChar()
    var conversionType = UNSET.toChar()
    val isPrecisionSet: Boolean
        get() = precision != UNSET

    val isWidthSet: Boolean
        get() = width != UNSET

    fun isFlagSet(flag: Int): Boolean {
        return 0 != flags and flag
    }

    fun getStrFlags(): String {
        return strFlags.toString()
    }

    fun setFlag(c: Char): Boolean {
        val newFlag = when (c) {
            '-' -> {
                FLAG_MINUS
            }
            '#' -> {
                FLAG_SHARP
            }
            '+' -> {
                FLAG_ADD
            }
            ' ' -> {
                FLAG_SPACE
            }
            '0' -> {
                FLAG_ZERO
            }
            ',' -> {
                FLAG_COMMA
            }
            '(' -> {
                FLAG_PARENTHESIS
            }
            else -> return false
        }
        flags = flags or newFlag
        strFlags.append(c)
        return true
    }

    fun requireArgument(): Boolean {
        return conversionType != '%' && conversionType != 'n'
    }

    companion object {
        const val LAST_ARGUMENT_INDEX = -2
        const val UNSET = -1
        const val FLAGS_UNSET = 0
        const val DEFAULT_PRECISION = 6
        const val FLAG_MINUS = 1
        const val FLAG_SHARP = 1 shl 1
        const val FLAG_ADD = 1 shl 2
        const val FLAG_SPACE = 1 shl 3
        const val FLAG_ZERO = 1 shl 4
        const val FLAG_COMMA = 1 shl 5
        const val FLAG_PARENTHESIS = 1 shl 6
        private const val FLAGT_TYPE_COUNT = 6
    }
}