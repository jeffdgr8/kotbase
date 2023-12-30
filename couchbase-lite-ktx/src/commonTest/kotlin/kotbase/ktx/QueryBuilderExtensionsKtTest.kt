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
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/test/java/com/molo17/couchbase/lite/QueryBuilderExtensionsKtTest.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Adapt for Kotlin/Common
 */

package kotbase.ktx

import kotbase.BaseDbTest
import kotbase.DataSource
import kotbase.Expression
import kotbase.Ordering
import kotbase.QueryBuilder
import kotbase.SelectResult
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Damiano Giusti on 26/03/2020.
 */
class QueryBuilderExtensionsKtTest : BaseDbTest() {

    ///////////////////////////////////////////////////////////////////////////
    // Projection
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun select_all() {
        val expected = QueryBuilder.select(SelectResult.all()).from(DataSource.collection(testCollection))
        val actual = select(all()) from testCollection
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_some_fields() {
        val expected = QueryBuilder
            .select(
                SelectResult.property("name"),
                SelectResult.property("age")
            )
            .from(DataSource.collection(testCollection))
        val actual = select("name", "age") from testCollection
        assertEquals(expected.explain(), actual.explain())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Selection
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun select_all_where_equalTo() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::equalTo)
        val actual = select(all()) from testCollection where { "type" equalTo "user" }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_notEqualTo() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::notEqualTo)
        val actual = select(all()) from testCollection where { "type" notEqualTo "user" }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_lessThan() {
        val expected = singleSelectionWhere("age", Expression.intValue(40), Expression::lessThan)
        val actual = select(all()) from testCollection where { "age" lessThan 40 }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_lessThanOrEqualTo() {
        val expected = singleSelectionWhere("age", Expression.intValue(40), Expression::lessThanOrEqualTo)
        val actual = select(all()) from testCollection where { "age" lessThanOrEqualTo 40 }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_greaterThan() {
        val expected = singleSelectionWhere("age", Expression.intValue(40), Expression::greaterThan)
        val actual = select(all()) from testCollection where { "age" greaterThan 40 }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_greaterThanOrEqualTo() {
        val expected = singleSelectionWhere("age", Expression.intValue(40), Expression::greaterThanOrEqualTo)
        val actual = select(all()) from testCollection where { "age" greaterThanOrEqualTo 40 }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_like() {
        val expected = singleSelectionWhere("name", Expression.string("dam"), Expression::like)
        val actual = select(all()) from testCollection where { "name" like "dam" }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_with_AND_condition() {
        val expected = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(
                Expression.property("type").equalTo(Expression.string("user"))
                    .and(Expression.property("name").equalTo(Expression.string("damiano")))
            )
        val actual = select(all())
            .from(testCollection)
            .where { ("type" equalTo "user") and ("name" equalTo "damiano") }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_with_two_AND_condition_joined_with_OR() {
        val expected = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(
                Expression.property("type").equalTo(Expression.string("user"))
                    .and(Expression.property("name").equalTo(Expression.string("damiano")))
                    .or(
                        Expression.property("type").equalTo(Expression.string("pet"))
                            .and(Expression.property("name").equalTo(Expression.string("kitty")))
                    )
            )
        val actual = select(all())
            .from(testCollection)
            .where {
                (("type" equalTo "user") and ("name" equalTo "damiano")) or
                    (("type" equalTo "pet") and ("name" equalTo "kitty"))
            }
        assertEquals(expected.explain(), actual.explain())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Selection data types
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun select_all_where_equalTo_String() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::equalTo)
        val actual = select(all()) from testCollection where { "type" equalTo "user" }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_int() {
        val expected = singleSelectionWhere("age", Expression.intValue(24), Expression::equalTo)
        val actual = select(all()) from testCollection where { "age" equalTo 24 }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_long() {
        val expected = singleSelectionWhere("age", Expression.longValue(24), Expression::equalTo)
        val actual = select(all()) from testCollection where { "age" equalTo 24L }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_float() {
        val expected = singleSelectionWhere("age", Expression.floatValue(24.50F), Expression::equalTo)
        val actual = select(all()) from testCollection where { "age" equalTo 24.50F }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_double() {
        val expected = singleSelectionWhere("age", Expression.doubleValue(24.50), Expression::equalTo)
        val actual = select(all()) from testCollection where { "age" equalTo 24.50 }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_boolean() {
        val expected = singleSelectionWhere("isBorn", Expression.booleanValue(true), Expression::equalTo)
        val actual = select(all()) from testCollection where { "isBorn" equalTo true }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_map() {
        val map = mutableMapOf<String, Any>("key" to "value")
        val expected = singleSelectionWhere("properties", Expression.map(map), Expression::equalTo)
        val actual = select(all()) from testCollection where { "properties" equalTo map }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_list() {
        val list = listOf("key", "value")
        val expected = singleSelectionWhere("channels", Expression.list(list), Expression::equalTo)
        val actual = select(all()) from testCollection where { "channels" equalTo list }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_equalTo_date() {
        val date = Clock.System.now()
        val expected = singleSelectionWhere("channels", Expression.date(date), Expression::equalTo)
        val actual = select(all()) from testCollection where { "channels" equalTo date }
        assertEquals(expected.explain(), actual.explain())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Ordering
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun select_all_where_orderBy_ascending() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::equalTo)
            .orderBy(Ordering.property("name").ascending())
        val actual = select(all())
            .from(testCollection)
            .where { "type" equalTo "user" }
            .orderBy { "name".ascending() }
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_orderBy_descending() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::equalTo)
            .orderBy(Ordering.property("name").descending())
        val actual = select(all())
            .from(testCollection)
            .where { "type" equalTo "user" }
            .orderBy { "name".descending() }
        assertEquals(expected.explain(), actual.explain())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Limit
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun select_all_where_limit() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::equalTo)
            .limit(Expression.intValue(1))
        val actual = select(all()).from(testCollection).where { "type" equalTo "user" }.limit(1)
        assertEquals(expected.explain(), actual.explain())
    }

    @Test
    fun select_all_where_orderBy_limit() {
        val expected = singleSelectionWhere("type", Expression.string("user"), Expression::equalTo)
            .orderBy(Ordering.property("name").ascending())
            .limit(Expression.intValue(1))
        val actual = select(all())
            .from(testCollection)
            .where { "type" equalTo "user" }
            .orderBy { "name".ascending() }
            .limit(1)
        assertEquals(expected.explain(), actual.explain())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////

    private inline fun singleSelectionWhere(field: String, valueExpression: Expression, operator: Expression.(Expression) -> Expression) =
        QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(testCollection))
            .where(Expression.property(field).operator(valueExpression))
}
