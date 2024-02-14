/*
 * MIT License
 *
 * Copyright (c) 2017 The JNanoID Authors
 * Copyright (c) 2017 Aventrix LLC
 * Copyright (c) 2017 Andrey Sitnik
 * Copyright (c) 2020 DatLag
 * Copyright (c) 2023 Jeff Lockhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/*
 * Based on:
 * Kotlin version: https://github.com/DATL4G/KMP-NanoId
 * Java version: https://github.com/aventrix/jnanoid
 * Original JavaScript version: https://github.com/ai/nanoid
 */

@file:JvmName("NanoId")
@file:Suppress("MemberVisibilityCanBePrivate")

package domain

import kotlin.experimental.and
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log
import kotlin.random.Random

/**
 * The default random number generator used by [randomNanoId].
 * Creates cryptographically strong NanoId Strings.
 */
val DEFAULT_NUMBER_GENERATOR: Random = Random

/**
 * The default alphabet used by [randomNanoId].
 * This alphabet uses `A-Za-z0-9_-` symbols.
 * Creates url-friendly NanoId Strings using 64 unique symbols.
 */
val DEFAULT_ALPHABET: CharArray = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

/**
 * The default size used by [randomNanoId].
 * Creates NanoId Strings with slightly more unique values than UUID v4.
 */
const val DEFAULT_SIZE: Int = 21

/**
 * Generate a unique NanoId String.
 *
 * The string is generated using the given random number generator.
 *
 * @param random   The random number generator.
 * @param alphabet The symbols used in the NanoId String.
 * @param size     The number of symbols in the NanoId String.
 * @return A randomly generated NanoId String.
 */
@JvmOverloads
fun randomNanoId(
    random: Random = DEFAULT_NUMBER_GENERATOR,
    alphabet: CharArray = DEFAULT_ALPHABET,
    size: Int = DEFAULT_SIZE
): String {
    require(alphabet.isNotEmpty() && alphabet.size < 256) { "Alphabet must contain between 1 and 255 symbols." }
    require(size > 0) { "Size must be greater than zero." }

    val mask = (2 shl floor(log((alphabet.size - 1).toDouble(), 2.0)).toInt()) - 1
    val step = ceil(1.6 * mask * size / alphabet.size).toInt()
    val idBuilder = StringBuilder()

    while (true) {
        val bytes = ByteArray(step)
        random.nextBytes(bytes)

        for (i in 0 until step) {
            val alphabetIndex = (bytes[i] and mask.toByte()).toInt()

            if (alphabetIndex < alphabet.size) {
                idBuilder.append(alphabet[alphabetIndex])

                if (idBuilder.length == size) {
                    return idBuilder.toString()
                }
            }
        }
    }
}
