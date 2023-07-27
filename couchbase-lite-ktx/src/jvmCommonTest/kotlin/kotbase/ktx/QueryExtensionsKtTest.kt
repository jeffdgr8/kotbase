/*
 * Copyright (c) 2020 MOLO17
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/test/java/com/molo17/couchbase/lite/QueryExtensionsKtTest.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Switch from Mockito to MockK for better Kotlin+Android support (can't mock Kotlin/Native yet)
 * - Use Kotbase addChangeListener(CoroutineContext, QueryChangeSuspendListener) API
 * - Move shared logic to TestQuery()
 */

package kotbase.ktx

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotbase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.CoroutineContext

/**
 * Created by Damiano Giusti on 19/03/2020.
 */
class QueryExtensionsKtTest {

    @Test
    fun when_query_succeeds_then_result_are_emitted_by_the_flow() = runBlocking {
        val expectedResultSet = mockk<ResultSet>()
        val queryChange = QueryChange(resultSet = expectedResultSet)
        val listenerToken = object : ListenerToken {}
        val queryUnderTest = TestQuery(this, queryChange, listenerToken)

        assertEquals(expectedResultSet, queryUnderTest.asFlow().first())
    }

    @Test
    fun when_query_fails_then_the_flow_fails() = runBlocking {
        val queryChange = QueryChange(error = TestException())
        val listenerToken = object : ListenerToken {}
        val queryUnderTest = TestQuery(this, queryChange, listenerToken)

        queryUnderTest
            .asFlow()
            .catch { error -> assertTrue(error is TestException) }
            .collect()
    }

    @Test
    fun when_the_flow_is_cancelled_then_the_query_is_stopped() = runBlocking {
        val expectedResultSet = mockk<ResultSet>()
        val queryChange = QueryChange(resultSet = expectedResultSet)
        val listenerToken = mockk<ListenerToken>()
        val queryUnderTest = TestQuery(this, queryChange, listenerToken)

        // Apply the `take(1)` operator for disposing the Flow after the first emission.
        queryUnderTest.asFlow().take(1).collect()

        verify { queryUnderTest.removeChangeListener(listenerToken) }
    }
}

private class TestException : RuntimeException()

private fun TestQuery(
    scope: CoroutineScope,
    queryChange: QueryChange,
    listenerToken: ListenerToken
): Query = mockk {
    every { addChangeListener(any<CoroutineContext>(), any()) } answers {
        val context = firstArg<CoroutineContext>()
        val listener = secondArg<QueryChangeSuspendListener>()
        scope.launch(context) { listener(queryChange) }
        listenerToken
    }
    every { execute() } returns mockk()
    every { removeChangeListener(any()) } just runs
}

private fun QueryChange(
    resultSet: ResultSet = mockk(),
    error: Throwable? = null
): QueryChange = mockk {
    every { this@mockk.results } returns resultSet
    every { this@mockk.error } returns error
}
