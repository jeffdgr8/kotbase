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
package kotbase

import kotlin.test.assertEquals

abstract class BaseQueryTest : BaseDbTest() {

    fun interface ResultVerifier {
        fun check(n: Int, result: Result)
    }

    protected fun loadDocuments(n: Int): List<MutableDocument> =
        loadDocuments(n, testCollection)

    protected fun loadDocuments(first: Int, n: Int): List<MutableDocument> =
        loadDocuments(first, n, testCollection)

    protected fun loadDocuments(n: Int, collection: Collection?): List<MutableDocument> =
        loadDocuments(1, n, collection)

    protected fun loadDocuments(first: Int, n: Int, collection: Collection?): List<MutableDocument> {
        val docs = createTestDocs(first, n)
        saveDocsInCollection(docs, collection!!)
        return docs
    }

    protected fun verifyQuery(query: Query, expected: Int, verifier: ResultVerifier) {
        assertEquals(expected, verifyQueryWithEnumerator(query, verifier))
        assertEquals(expected, verifyQueryWithIterable(query, verifier))
    }

    protected fun verifyQueryWithEnumerator(query: Query, verifier: ResultVerifier): Int {
        var n = 0
        try {
            query.execute().use { rs ->
                while (true) {
                    val result = rs.next() ?: break
                    verifier.check(++n, result)
                }
            }
        } catch (e: Exception) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Failed verifying query (enumerator)", e)
        }
        return n
    }

    private fun verifyQueryWithIterable(query: Query, verifier: ResultVerifier): Int {
        var n = 0
        try {
            query.execute().use { rs ->
                for (result in rs) {
                    verifier.check(++n, result)
                }
            }
        } catch (e: Exception) {
            // Cause isn't logged on native platforms...
            // https://youtrack.jetbrains.com/issue/KT-62794
            println("Cause:")
            println(e.message)
            println(e.stackTraceToString())
            throw AssertionError("Failed verifying query (iterable)", e)
        }
        return n
    }
}
