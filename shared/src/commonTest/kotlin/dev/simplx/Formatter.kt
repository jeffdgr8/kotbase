@file:Suppress("UNCHECKED_CAST")

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
class Formatter {
    private val out: StringBuilder = StringBuilder()

    fun format(format: String, vararg args: Any?): Formatter {
        val formatBuffer = CharArrayBuffer(format.toCharArray())
        val parser = ParserStateMachine(formatBuffer)
        val transformer = Transformer(this)
        var currentObjectIndex = 0
        var lastArgument: Any? = null
        var hasLastArgumentSet = false
        while (formatBuffer.hasRemaining()) {
            parser.reset()
            val token = parser.nextFormatToken
            var result: String?
            var plainText = token.plainText
            if (token.conversionType == FormatToken.UNSET.toChar()) {
                result = plainText
            } else {
                plainText = plainText!!.substring(0, plainText.indexOf('%'))
                var argument: Any? = null
                if (token.requireArgument()) {
                    val index = if (token.argIndex == FormatToken.UNSET) currentObjectIndex++ else token.argIndex
                    argument = getArgument(
                        index, token, lastArgument,
                        hasLastArgumentSet, args.toList()
                    )
                    lastArgument = argument
                    hasLastArgumentSet = true
                }
                result = plainText + transformer.transform(token, argument)
            }
            // if output is made by formattable callback
            if (null != result) {
                try {
                    out.append(result)
                } catch (e: Exception) {
                }
            }
        }
        return this
    }

    private fun getArgument(
        index: Int,
        token: FormatToken,
        lastArgument: Any?,
        hasLastArgumentSet: Boolean,
        args: List<Any?>
    ): Any? {
        val lst = args[0] as Array<Any>?

        if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
            throw Exception("<") //$NON-NLS-1$
        }
        if (null == lst) {
            return null
        }
        if (index >= lst.size) {
            throw Exception(token.plainText)
        }
        return if (index == FormatToken.LAST_ARGUMENT_INDEX) {
            lastArgument
        } else lst[index]
    }

    override fun toString(): String {
        return out.toString()
    }
}