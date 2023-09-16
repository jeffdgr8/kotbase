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
package kotbase.test

import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

fun assertIntContentEquals(expected: Array<Int?>?, actual: Array<Any?>?, message: String? = null) {
    @Suppress("UNCHECKED_CAST")
    assertContentEquals(
        expected as Array<Any?>,
        actual?.map { it?.longToInt() }?.toTypedArray(),
        message
    )
}

fun assertIntContentEquals(
    expected: Iterable<Int?>?,
    actual: Iterable<Any?>?,
    message: String? = null
) {
    assertContentEquals(expected, actual?.map { it?.longToInt() }, message)
}

fun assertIntEquals(expected: Any?, actual: Any?, message: String? = null) {
    assertEquals(expected, actual?.longToInt(), message)
}

@Suppress("UNCHECKED_CAST")
private fun Any.longToInt(): Any = when (this) {
    is Long -> toInt()
    is Map<*, *> -> (this as Map<String, Any?>).longToInt()
    is List<*> -> longToInt()
    else -> this
}

private fun Map<String, Any?>.longToInt(): Map<String, Any?> =
    mapValues { it.value?.longToInt() }

private fun List<Any?>.longToInt(): List<Any?> =
    map { it?.longToInt() }
