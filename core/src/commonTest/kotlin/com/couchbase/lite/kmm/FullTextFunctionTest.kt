package com.couchbase.lite.kmm

import com.couchbase.lite.asJSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FullTextFunctionTest {

    @Test
    fun testRank() {
        val expr = FullTextFunction.rank("abc")
        assertNotNull(expr)
        val obj = expr.asJSON()
        assertNotNull(obj)
        assertTrue(obj is List<*>)
        assertEquals(listOf("RANK()", "abc"), obj)
    }
}
