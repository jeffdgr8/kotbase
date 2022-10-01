package com.udobny.kmp.test

import kotlin.test.assertContentEquals

fun assertIntContentEquals(expected: Array<Int?>?, actual: Array<Any?>?, message: String? = null) {
    assertContentEquals(expected, actual?.map { (it as? Number)?.toInt() }?.toTypedArray(), message)
}
