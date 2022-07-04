@file:Suppress("unused", "UNUSED_VALUE", "LocalVariableName", "CascadeIf")

package dev.simplx

import kotlin.math.max
import kotlin.math.min


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
class Transformer internal constructor(private val formatter: Formatter) {
    private var formatToken: FormatToken? = null
    private var arg: Any? = null

    /*
         * Gets the formatted string according to the format token and the
         * argument.
         */
    fun transform(token: FormatToken, argument: Any?): String { /* init data member to print */
        formatToken = token
        arg = argument
        val result: String = when (token.conversionType) {
            'S', 's' -> {
                transformFromString()
            }
            'd', 'o', 'x', 'X' -> {
                transformFromInteger()
            }
            '%' -> {
                transformFromPercent()
            }
            else -> {
                throw Exception(
                    token.conversionType.toString()
                )
            }
        }
        if (Character.isUpperCase(token.conversionType)) {
            return result.uppercase()
        }
        return result
    }


    /*
         * Transforms the String to a formatted string.
         */
    private fun transformFromString(): String {
        val result = StringBuilder()
        val startIndex = 0
        val flags = formatToken!!.flags
        if (formatToken!!.isFlagSet(FormatToken.FLAG_MINUS)
            && !formatToken!!.isWidthSet
        ) {
            throw Exception(
                "-" //$NON-NLS-1$
                        + formatToken!!.conversionType
            )
        }
        // only '-' is valid for flags if the argument is not an
        // instance of Formattable
        if (FormatToken.FLAGS_UNSET != flags
            && FormatToken.FLAG_MINUS != flags
        ) {
            throw Exception(
            )
        }
        result.append(arg)
        return padding(result, startIndex)
    }

    /**
     * Transforms percent to a formatted string. Only '-' is legal flag.
     * Precision is illegal.
     */
    private fun transformFromPercent(): String {
        val result = StringBuilder("%") //$NON-NLS-1$
        val startIndex = 0
        val flags = formatToken!!.flags
        if (formatToken!!.isFlagSet(FormatToken.FLAG_MINUS)
            && !formatToken!!.isWidthSet
        ) {
            throw Exception(
                "-" //$NON-NLS-1$
                        + formatToken!!.conversionType
            )
        }
        if (FormatToken.FLAGS_UNSET != flags
            && FormatToken.FLAG_MINUS != flags
        ) {
            throw Exception(
            )
        }
        if (formatToken!!.isPrecisionSet) {
            throw Exception(
            )
        }
        return padding(result, startIndex)
    }

    /**
     * Pads characters to the formatted string.
     */
    private fun padding(sourc: StringBuilder, startIndex: Int): String {
        var source = sourc
        var start = startIndex
        val paddingRight = formatToken
            ?.isFlagSet(FormatToken.FLAG_MINUS)
        var paddingChar = '\u0020' // space as padding char.
        if (formatToken!!.isFlagSet(FormatToken.FLAG_ZERO)) {
            paddingChar = '0'
        } else { // if padding char is space, always padding from the head
            // location.
            start = 0
        }
        var width = formatToken!!.width
        val precision = formatToken!!.precision
        var length = source.length
        if (precision >= 0) {
            length = min(length, precision)
            source = StringBuilder(source.substring(0, length) + source.substring(source.length))
        }
        if (width > 0) {
            width = max(source.length, width)
        }
        if (length >= width) {
            return source.toString()
        }
        val paddings = CharArray(width - length) { paddingChar }
        val insertString = paddings.concatToString()
        if (paddingRight!!) {
            source.append(insertString)
        } else {
            source.insert(start, insertString)
        }
        return source.toString()
    }

    /**
     * Transforms the Integer to a formatted string.
     */
    private fun transformFromInteger(): String {
        var startIndex = 0
        var isNegative = false
        var result = StringBuilder()
        val currentConversionType = formatToken!!.conversionType
        var value: Long
        if (formatToken!!.isFlagSet(FormatToken.FLAG_MINUS)
            || formatToken!!.isFlagSet(FormatToken.FLAG_ZERO)
        ) {
            if (!formatToken!!.isWidthSet) {
                throw Exception(
                )
            }
        }
        // Combination of '+' & ' ' is illegal.
        if (formatToken!!.isFlagSet(FormatToken.FLAG_ADD)
            && formatToken!!.isFlagSet(FormatToken.FLAG_SPACE)
        ) {
            throw Exception(formatToken!!.getStrFlags())
        }
        if (formatToken!!.isPrecisionSet) {
            throw Exception(
            )
        }
        value = if (arg is Long) {
            (arg as Long).toLong()
        } else if (arg is Int) {
            (arg as Int).toLong()
        } else if (arg is Short) {
            (arg as Short).toLong()
        } else if (arg is Byte) {
            (arg as Byte).toLong()
        } else {
            error("Value not supported [$arg] for type `$currentConversionType'")
        }
        if ('d' != currentConversionType) {
            if (formatToken!!.isFlagSet(FormatToken.FLAG_ADD)
                || formatToken!!.isFlagSet(FormatToken.FLAG_SPACE)
                || formatToken!!.isFlagSet(FormatToken.FLAG_COMMA)
                || formatToken!!.isFlagSet(FormatToken.FLAG_PARENTHESIS)
            ) {
                throw Exception(
                )
            }
        }
        if (formatToken!!.isFlagSet(FormatToken.FLAG_SHARP)) {
            startIndex += if ('d' == currentConversionType) {
                throw Exception(
                )
            } else if ('o' == currentConversionType) {
                result.append("0") //$NON-NLS-1$
                1
            } else {
                result.append("0x") //$NON-NLS-1$
                2
            }
        }
        if (formatToken!!.isFlagSet(FormatToken.FLAG_MINUS)
            && formatToken!!.isFlagSet(FormatToken.FLAG_ZERO)
        ) {
            throw Exception(formatToken!!.getStrFlags())
        }
        if (value < 0) {
            isNegative = true
        }
        if ('d' == currentConversionType) {
            result.append(arg.toString())
        } else {
            val BYTE_MASK = 0x00000000000000FFL
            val SHORT_MASK = 0x000000000000FFFFL
            val INT_MASK = 0x00000000FFFFFFFFL
            if (isNegative) {
                if (arg is Byte) {
                    value = value and BYTE_MASK
                } else if (arg is Short) {
                    value = value and SHORT_MASK
                } else if (arg is Int) {
                    value = value and INT_MASK
                }
            }
            isNegative = false
        }
        if (!isNegative) {
            if (formatToken!!.isFlagSet(FormatToken.FLAG_ADD)) {
                result.insert(0, '+')
                startIndex += 1
            }
            if (formatToken!!.isFlagSet(FormatToken.FLAG_SPACE)) {
                result.insert(0, ' ')
                startIndex += 1
            }
        }
        /* pad paddingChar to the output */if (isNegative
            && formatToken!!.isFlagSet(FormatToken.FLAG_PARENTHESIS)
        ) {
            result = wrapParentheses(result)
            return result.toString()
        }
        if (isNegative && formatToken!!.isFlagSet(FormatToken.FLAG_ZERO)) {
            startIndex++
        }
        return padding(result, startIndex)
    }

    /**
     * add () to the output,if the value is negative and
     * formatToken.FLAG_PARENTHESIS is set. 'result' is used as an in-out
     * parameter.
     */
    private fun wrapParentheses(result: StringBuilder): StringBuilder { // delete the '-'
        result.deleteAt(0)
        result.insert(0, '(')
        if (formatToken!!.isFlagSet(FormatToken.FLAG_ZERO)) {
            formatToken!!.width = formatToken!!.width - 1
            padding(result, 1)
            result.append(')')
        } else {
            result.append(')')
            padding(result, 0)
        }
        return result
    }
}