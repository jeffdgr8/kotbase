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
 * Modified by Jeff Lockhart
 *
 * - Use kotbase package for couchbase-lite-kmp Kotlin Multiplatform bindings
 * - Switch from Mockito to MockK for better Kotlin+Android support (can't mock Kotlin/Native yet)
 */

package kotbase.molo17

import io.mockk.*
import kotbase.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by Damiano Giusti on 19/03/2020.
 */
class QueryExtensionsKtTest {

    @Test
    fun when_query_succeeds_then_result_are_emitted_by_the_flow() = runBlocking {
        val expectedResultSet = mockk<ResultSet>()
        val queryUnderTest = TestQuery(addChangeListenerCalled = { listener ->
            val queryChange = QueryChange(resultSet = expectedResultSet)
            listener(queryChange)
            object : ListenerToken {}
        })

        assertEquals(expectedResultSet, queryUnderTest.asFlow().first())
    }

    @Test
    fun when_query_fails_then_the_flow_fails() = runBlocking {
        val queryUnderTest = TestQuery(addChangeListenerCalled = { listener ->
            val queryChange = QueryChange(error = TestException())
            listener(queryChange)
            object : ListenerToken {}
        })

        queryUnderTest
            .asFlow()
            .catch { error -> assertTrue(error is TestException) }
            .collect()
    }

    @Test
    fun when_the_flow_is_cancelled_then_the_query_is_stopped() = runBlocking {
        val listenerToken = mockk<ListenerToken>()
        val expectedResultSet = mockk<ResultSet>()
        val queryUnderTest = TestQuery(addChangeListenerCalled = { listener ->
            val queryChange = QueryChange(resultSet = expectedResultSet)
            listener(queryChange)
            listenerToken
        })

        // Apply the `take(1)` operator for disposing the Flow after the first emission.
        queryUnderTest.asFlow().take(1).collect()

        verify { queryUnderTest.removeChangeListener(listenerToken) }
    }
}

private class TestException : RuntimeException()

private fun TestQuery(
    addChangeListenerCalled: (QueryChangeListener) -> ListenerToken
): Query = mockk {
    every { addChangeListener(any()) } answers  {
        val listener = firstArg<QueryChangeListener>()
        addChangeListenerCalled(listener)
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