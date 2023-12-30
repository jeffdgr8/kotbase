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
package kotbase.ktx

import kotbase.BaseDbTest
import kotbase.DataSource
import kotbase.Expression
import kotbase.Function
import kotbase.Meta
import kotbase.QueryBuilder
import kotbase.SelectResult
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryBuilderExtTest : BaseDbTest() {

    @Test
    fun select_expression_properties() {
        val expected = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.property("foo"),
            SelectResult.property("bar")
        ).from(DataSource.collection(testCollection))

        val actual = select(Meta.id, "foo", "bar") from testCollection

        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_expression_results() {
        val expected = QueryBuilder.select(
            SelectResult.expression(Meta.id),
            SelectResult.all()
        ).from(DataSource.collection(testCollection))

        val actual = select(Meta.id, all()) from testCollection

        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_count() {
        val expected = QueryBuilder.select(
            SelectResult.expression(
                Function.count(Expression.string("*"))
            )
        ).from(DataSource.collection(testCollection))

        val actual = selectCount() from testCollection

        assertEquals(expected.explain(), actual.explain())
    }
}
