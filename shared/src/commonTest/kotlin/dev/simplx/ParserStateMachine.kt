package dev.simplx

import dev.simplx.Character.isDigit

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
class ParserStateMachine(private val format: CharArrayBuffer) {
    private var token: FormatToken? = null
    private var state = ENTRY_STATE
    private var currentChar = 0.toChar()
    fun reset() {
        currentChar = FormatToken.UNSET.toChar()
        state = ENTRY_STATE
        token = null
    }// exit state does not need to get next char// FINITE AUTOMATIC MACHINE

    /**
     * Gets the information about the current format token. Information is
     * recorded in the FormatToken returned and the position of the stream
     * for the format string will be advanced till the next format token.
     */
    val nextFormatToken: FormatToken
        get() {
            token = FormatToken()
            token!!.formatStringStartIndex = format.position()
            // FINITE AUTOMATIC MACHINE
            while (true) {
                if (EXIT_STATE != state) { // exit state does not need to get next char
                    currentChar = nextFormatChar
                    if (EOS == currentChar
                        && ENTRY_STATE != state
                    ) {
                        throw Exception(
                            formatString
                        )
                    }
                }
                when (state) {
                    EXIT_STATE -> {
                        process_EXIT_STATE()
                        return token as FormatToken
                    }
                    ENTRY_STATE -> {
                        process_ENTRY_STATE()
                    }
                    START_CONVERSION_STATE -> {
                        process_START_CONVERSION_STATE()
                    }
                    FLAGS_STATE -> {
                        process_FlAGS_STATE()
                    }
                    WIDTH_STATE -> {
                        process_WIDTH_STATE()
                    }
                    PRECISION_STATE -> {
                        process_PRECISION_STATE()
                    }
                    CONVERSION_TYPE_STATE -> {
                        process_CONVERSION_TYPE_STATE()
                    }
                    SUFFIX_STATE -> {
                        process_SUFFIX_STATE()
                    }
                }
            }
        }

    /**
     * Gets next char from the format string.
     */
    private val nextFormatChar: Char
        get() = if (format.hasRemaining()) {
            format.get()
        } else EOS

    private val formatString: String
        get() {
            val end: Int = format.position()
            format.rewind()
            val formatString: String = format.subSequence(
                token!!.formatStringStartIndex, end
            ).toString()
            format.position(end)
            return formatString
        }

    private fun process_ENTRY_STATE() {
        if (EOS == currentChar) {
            state = EXIT_STATE
        } else if ('%' == currentChar) { // change to conversion type state
            state = START_CONVERSION_STATE
        }
        // else remains in ENTRY_STATE
    }

    private fun process_START_CONVERSION_STATE() {
        if (isDigit(currentChar)) {
            val position: Int = format.position() - 1
            val number = parseInt(format)
            var nextChar = 0.toChar()
            if (format.hasRemaining()) {
                nextChar = format.get()
            }
            if ('$' == nextChar) { // the digital sequence stands for the argument
// index.
                // k$ stands for the argument whose index is k-1 except that
// 0$ and 1$ both stands for the first element.
                if (number > 0) {
                    token!!.argIndex = number - 1
                } else if (number == FormatToken.UNSET) {
                    throw Exception(
                        formatString
                    )
                }
                state = FLAGS_STATE
            } else { // the digital zero stands for one format flag.
                if ('0' == currentChar) {
                    state = FLAGS_STATE
                    format.position(position)
                } else { // the digital sequence stands for the width.
                    state = WIDTH_STATE
                    // do not get the next char.
                    format.position(format.position() - 1)
                    token!!.width = number
                }
            }
            currentChar = nextChar
        } else if ('<' == currentChar) {
            state = FLAGS_STATE
            token!!.argIndex = FormatToken.LAST_ARGUMENT_INDEX
        } else {
            state = FLAGS_STATE
            // do not get the next char.
            format.position(format.position() - 1)
        }
    }

    private fun process_FlAGS_STATE() {
        if (token!!.setFlag(currentChar)) { // remains in FLAGS_STATE
        } else if (isDigit(currentChar)) {
            token!!.width = parseInt(format)
            state = WIDTH_STATE
        } else if ('.' == currentChar) {
            state = PRECISION_STATE
        } else {
            state = CONVERSION_TYPE_STATE
            // do not get the next char.
            format.position(format.position() - 1)
        }
    }

    private fun process_WIDTH_STATE() {
        if ('.' == currentChar) {
            state = PRECISION_STATE
        } else {
            state = CONVERSION_TYPE_STATE
            // do not get the next char.
            format.position(format.position() - 1)
        }
    }

    private fun process_PRECISION_STATE() {
        if (isDigit(currentChar)) {
            token!!.precision = parseInt(format)
        } else { // the precision is required but not given by the
// format string.
            throw Exception(formatString)
        }
        state = CONVERSION_TYPE_STATE
    }

    private fun process_CONVERSION_TYPE_STATE() {
        token!!.conversionType = currentChar
        state = if ('t' == currentChar || 'T' == currentChar) {
            SUFFIX_STATE
        } else {
            EXIT_STATE
        }
    }

    private fun process_SUFFIX_STATE() {
        token!!.dateSuffix = currentChar
        state = EXIT_STATE
    }

    private fun process_EXIT_STATE() {
        token!!.plainText = formatString
    }

    /**
     * Parses integer value from the given buffer
     */
    private fun parseInt(buffer: CharArrayBuffer): Int {
        val start: Int = buffer.position() - 1
        var end: Int = buffer.limit()
        while (buffer.hasRemaining()) {
            if (!isDigit(buffer.get())) {
                end = buffer.position() - 1
                break
            }
        }
        buffer.position(0)
        val intStr: String = buffer.subSequence(start, end).toString()
        buffer.position(end)
        return try {
            intStr.toInt()
        } catch (e: NumberFormatException) {
            FormatToken.UNSET
        }
    }

    companion object {
        private const val EOS = (-1).toChar()
        private const val EXIT_STATE = 0
        private const val ENTRY_STATE = 1
        private const val START_CONVERSION_STATE = 2
        private const val FLAGS_STATE = 3
        private const val WIDTH_STATE = 4
        private const val PRECISION_STATE = 5
        private const val CONVERSION_TYPE_STATE = 6
        private const val SUFFIX_STATE = 7
    }
}