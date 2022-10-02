package com.udobny.kmp.test

import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

fun assertIntContentEquals(expected: Array<Int?>?, actual: Array<Any?>?, message: String? = null) {
    @Suppress("UNCHECKED_CAST")
    assertContentEquals(expected as Array<Any?>, actual?.map { it?.intIfLong() }?.toTypedArray(), message)
}

fun assertIntEquals(expected: Int, actual: Any?, message: String? = null) {
    assertEquals(expected, actual?.intIfLong(), message)
}

private fun Any.intIfLong(): Any =
    (this as? Int) ?: (this as? Long)?.toInt() ?: this

fun assertIntMapEquals(expected: Map<String, Any?>?, actual: Map<String, Any?>?, message: String? = null) {
    val intActual = actual?.mapValues { (_, value) ->
        when (value) {
            is Long -> value.toInt()
            else -> value
        }
    }
    assertEquals(expected, intActual, message)
}
