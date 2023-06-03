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

private fun Any.longToInt(): Any {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Long -> toInt()
        is Map<*, *> -> (this as Map<String, Any?>).longToInt()
        is List<*> -> longToInt()
        else -> this
    }
}

private fun Map<String, Any?>.longToInt(): Map<String, Any?> =
    mapValues { it.value?.longToInt() }

private fun List<Any?>.longToInt(): List<Any?> =
    map { it?.longToInt() }
