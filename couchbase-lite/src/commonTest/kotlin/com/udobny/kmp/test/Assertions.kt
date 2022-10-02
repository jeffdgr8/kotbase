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
