package com.couchbase.lite.kmp

import kotlin.test.assertEquals

@OptIn(ExperimentalStdlibApi::class)
abstract class BaseQueryTest : BaseDbTest() {

    fun interface QueryResult {
        fun check(n: Int, result: Result)
    }

    protected fun createNumberedDocInBaseTestDb(i: Int, num: Int): String {
        val doc = MutableDocument("doc$i")
        doc.setValue("number1", i)
        doc.setValue("number2", num - i)
        return saveDocInBaseTestDb(doc).id
    }

    protected fun loadNumberedDocs(num: Int): List<Map<String, Any?>> {
        return loadNumberedDocs(1, num)
    }

    protected fun loadNumberedDocs(from: Int, to: Int): List<Map<String, Any?>> {
        val numbers = mutableListOf<Map<String, Any?>>()
        baseTestDb.inBatch {
            for (i in from..to) {
                numbers.add(
                    baseTestDb.getDocument(createNumberedDocInBaseTestDb(i, to))!!.toMap()
                )
            }
        }
        return numbers
    }

    protected fun verifyQuery(query: Query, result: QueryResult): Int {
        return verifyQuery(query, true, result)
    }

    protected fun verifyQuery(query: Query, runBoth: Boolean, result: QueryResult): Int {
        val counter1 = verifyQueryWithEnumerator(query, result)
        if (runBoth) {
            val counter2 = verifyQueryWithIterable(query, result)
            assertEquals(counter1.toLong(), counter2.toLong())
        }
        return counter1
    }

    private fun verifyQueryWithEnumerator(query: Query, queryResult: QueryResult): Int {
        var n = 0
        query.execute().use { rs ->
            while (true) {
                val result = rs.next() ?: break
                queryResult.check(++n, result)
            }
        }
        return n
    }

    private fun verifyQueryWithIterable(query: Query, queryResult: QueryResult): Int {
        var n = 0
        query.execute().use { rs ->
            for (result in rs) {
                queryResult.check(++n, result)
            }
        }
        return n
    }
}
