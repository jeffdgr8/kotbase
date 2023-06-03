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
 * - Use com.couchbase.lite.kmp package for couchbase-lite-kmp Kotlin Multiplatform bindings
 * - Resolve explicitApiWarning() requirements
 * - Add support for additional Query subclasses
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.molo17.couchbase.lite.kmp

import com.couchbase.lite.kmp.*
import kotlinx.datetime.Instant
import kotlin.Array

public inline fun select(vararg expressions: SelectResult): Select = QueryBuilder.select(*expressions)
public inline fun select(vararg keys: String): Select = QueryBuilder.select(*keys.map(SelectResult::property).toTypedArray())

public inline fun all(): SelectResult.From = SelectResult.all()

public inline infix fun Select.from(database: Database): From = from(DataSource.database(database))

public inline infix fun From.where(builder: WhereBuilder.() -> Expression): Where = where(WhereBuilder().builder())
public inline infix fun Joins.where(builder: WhereBuilder.() -> Expression): Where = where(WhereBuilder().builder())

public inline fun From.orderBy(builder: OrderByBuilder.() -> Unit): OrderBy = orderBy(*OrderByBuilder().apply(builder).orderings())
public inline fun Joins.orderBy(builder: OrderByBuilder.() -> Unit): OrderBy = orderBy(*OrderByBuilder().apply(builder).orderings())
public inline fun Where.orderBy(builder: OrderByBuilder.() -> Unit): OrderBy = orderBy(*OrderByBuilder().apply(builder).orderings())
public inline fun GroupBy.orderBy(builder: OrderByBuilder.() -> Unit): OrderBy = orderBy(*OrderByBuilder().apply(builder).orderings())
public inline fun Having.orderBy(builder: OrderByBuilder.() -> Unit): OrderBy = orderBy(*OrderByBuilder().apply(builder).orderings())

public inline fun From.limit(count: Int, offset: Int? = null): Limit = limit(Expression.intValue(count), offset?.let(Expression::intValue))
public inline fun Joins.limit(count: Int, offset: Int? = null): Limit = limit(Expression.intValue(count), offset?.let(Expression::intValue))
public inline fun Where.limit(count: Int, offset: Int? = null): Limit = limit(Expression.intValue(count), offset?.let(Expression::intValue))
public inline fun GroupBy.limit(count: Int, offset: Int? = null): Limit = limit(Expression.intValue(count), offset?.let(Expression::intValue))
public inline fun Having.limit(count: Int, offset: Int? = null): Limit = limit(Expression.intValue(count), offset?.let(Expression::intValue))
public inline fun OrderBy.limit(count: Int, offset: Int? = null): Limit = limit(Expression.intValue(count), offset?.let(Expression::intValue))

public inline fun not(expression: Expression): Expression = Expression.not(expression)
public inline fun property(name: String): PropertyExpression = Expression.property(name)

public inline fun Expression.lessThan(string: String): Expression = lessThan(Expression.string(string))
public inline fun Expression.lessThan(int: Int): Expression = lessThan(Expression.intValue(int))
public inline fun Expression.lessThan(long: Long): Expression = lessThan(Expression.longValue(long))
public inline fun Expression.lessThan(float: Float): Expression = lessThan(Expression.floatValue(float))
public inline fun Expression.lessThan(double: Double): Expression = lessThan(Expression.doubleValue(double))
public inline fun Expression.lessThan(boolean: Boolean): Expression = lessThan(Expression.booleanValue(boolean))
public inline fun Expression.lessThan(date: Instant): Expression = lessThan(Expression.date(date))

public inline fun Expression.lessThanOrEqualTo(string: String): Expression = lessThanOrEqualTo(Expression.string(string))
public inline fun Expression.lessThanOrEqualTo(int: Int): Expression = lessThanOrEqualTo(Expression.intValue(int))
public inline fun Expression.lessThanOrEqualTo(long: Long): Expression = lessThanOrEqualTo(Expression.longValue(long))
public inline fun Expression.lessThanOrEqualTo(float: Float): Expression = lessThanOrEqualTo(Expression.floatValue(float))
public inline fun Expression.lessThanOrEqualTo(double: Double): Expression = lessThanOrEqualTo(Expression.doubleValue(double))
public inline fun Expression.lessThanOrEqualTo(boolean: Boolean): Expression = lessThanOrEqualTo(Expression.booleanValue(boolean))
public inline fun Expression.lessThanOrEqualTo(date: Instant): Expression = lessThanOrEqualTo(Expression.date(date))

public inline fun Expression.greaterThan(string: String): Expression = greaterThan(Expression.string(string))
public inline fun Expression.greaterThan(int: Int): Expression = greaterThan(Expression.intValue(int))
public inline fun Expression.greaterThan(long: Long): Expression = greaterThan(Expression.longValue(long))
public inline fun Expression.greaterThan(float: Float): Expression = greaterThan(Expression.floatValue(float))
public inline fun Expression.greaterThan(double: Double): Expression = greaterThan(Expression.doubleValue(double))
public inline fun Expression.greaterThan(boolean: Boolean): Expression = greaterThan(Expression.booleanValue(boolean))
public inline fun Expression.greaterThan(date: Instant): Expression = greaterThan(Expression.date(date))

public inline fun Expression.greaterThanOrEqualTo(string: String): Expression = greaterThanOrEqualTo(Expression.string(string))
public inline fun Expression.greaterThanOrEqualTo(int: Int): Expression = greaterThanOrEqualTo(Expression.value(int))
public inline fun Expression.greaterThanOrEqualTo(long: Long): Expression = greaterThanOrEqualTo(Expression.longValue(long))
public inline fun Expression.greaterThanOrEqualTo(float: Float): Expression = greaterThanOrEqualTo(Expression.floatValue(float))
public inline fun Expression.greaterThanOrEqualTo(double: Double): Expression = greaterThanOrEqualTo(Expression.doubleValue(double))
public inline fun Expression.greaterThanOrEqualTo(boolean: Boolean): Expression = greaterThanOrEqualTo(Expression.booleanValue(boolean))
public inline fun Expression.greaterThanOrEqualTo(date: Instant): Expression = greaterThanOrEqualTo(Expression.date(date))

public inline fun Expression.equalTo(string: String): Expression = equalTo(Expression.string(string))
public inline fun Expression.equalTo(int: Int): Expression = equalTo(Expression.intValue(int))
public inline fun Expression.equalTo(long: Long): Expression = equalTo(Expression.longValue(long))
public inline fun Expression.equalTo(float: Float): Expression = equalTo(Expression.floatValue(float))
public inline fun Expression.equalTo(double: Double): Expression = equalTo(Expression.doubleValue(double))
public inline fun Expression.equalTo(boolean: Boolean): Expression = equalTo(Expression.booleanValue(boolean))
public inline fun Expression.equalTo(date: Instant): Expression = equalTo(Expression.date(date))
public inline fun Expression.equalTo(map: Map<String, Any?>): Expression = equalTo(Expression.map(map))
public inline fun Expression.equalTo(list: List<Any>): Expression = equalTo(Expression.list(list))

public inline fun Expression.notEqualTo(string: String): Expression = notEqualTo(Expression.string(string))
public inline fun Expression.notEqualTo(int: Int): Expression = notEqualTo(Expression.intValue(int))
public inline fun Expression.notEqualTo(long: Long): Expression = notEqualTo(Expression.longValue(long))
public inline fun Expression.notEqualTo(float: Float): Expression = notEqualTo(Expression.floatValue(float))
public inline fun Expression.notEqualTo(double: Double): Expression = notEqualTo(Expression.doubleValue(double))
public inline fun Expression.notEqualTo(boolean: Boolean): Expression = notEqualTo(Expression.booleanValue(boolean))
public inline fun Expression.notEqualTo(date: Instant): Expression = notEqualTo(Expression.date(date))
public inline fun Expression.notEqualTo(map: Map<String, Any?>): Expression = notEqualTo(Expression.map(map))
public inline fun Expression.notEqualTo(list: List<Any>): Expression = notEqualTo(Expression.list(list))

public inline fun Expression.like(string: String): Expression = like(Expression.string(string))
public inline fun Expression.like(int: Int): Expression = like(Expression.intValue(int))
public inline fun Expression.like(long: Long): Expression = like(Expression.longValue(long))
public inline fun Expression.like(float: Float): Expression = like(Expression.floatValue(float))
public inline fun Expression.like(double: Double): Expression = like(Expression.doubleValue(double))
public inline fun Expression.like(boolean: Boolean): Expression = like(Expression.booleanValue(boolean))
public inline fun Expression.like(date: Instant): Expression = like(Expression.date(date))

public inline infix fun Expression.and(other: Expression): Expression = and(other)
public inline infix fun Expression.or(other: Expression): Expression = or(other)

public class WhereBuilder {
    public inline infix fun String.lessThan(expression: Expression): Expression = property(this).lessThan(expression)
    public inline infix fun String.lessThan(string: String): Expression = property(this).lessThan(string)
    public inline infix fun String.lessThan(int: Int): Expression = property(this).lessThan(int)
    public inline infix fun String.lessThan(long: Long): Expression = property(this).lessThan(long)
    public inline infix fun String.lessThan(float: Float): Expression = property(this).lessThan(float)
    public inline infix fun String.lessThan(double: Double): Expression = property(this).lessThan(double)
    public inline infix fun String.lessThan(boolean: Boolean): Expression = property(this).lessThan(boolean)
    public inline infix fun String.lessThan(date: Instant): Expression = property(this).lessThan(date)

    public inline infix fun String.lessThanOrEqualTo(expression: Expression): Expression = property(this).lessThanOrEqualTo(expression)
    public inline infix fun String.lessThanOrEqualTo(string: String): Expression = property(this).lessThanOrEqualTo(string)
    public inline infix fun String.lessThanOrEqualTo(int: Int): Expression = property(this).lessThanOrEqualTo(int)
    public inline infix fun String.lessThanOrEqualTo(long: Long): Expression = property(this).lessThanOrEqualTo(long)
    public inline infix fun String.lessThanOrEqualTo(float: Float): Expression = property(this).lessThanOrEqualTo(float)
    public inline infix fun String.lessThanOrEqualTo(double: Double): Expression = property(this).lessThanOrEqualTo(double)
    public inline infix fun String.lessThanOrEqualTo(boolean: Boolean): Expression = property(this).lessThanOrEqualTo(boolean)
    public inline infix fun String.lessThanOrEqualTo(date: Instant): Expression = property(this).lessThanOrEqualTo(date)

    public inline infix fun String.greaterThan(expression: Expression): Expression = property(this).greaterThan(expression)
    public inline infix fun String.greaterThan(string: String): Expression = property(this).greaterThan(string)
    public inline infix fun String.greaterThan(int: Int): Expression = property(this).greaterThan(int)
    public inline infix fun String.greaterThan(long: Long): Expression = property(this).greaterThan(long)
    public inline infix fun String.greaterThan(float: Float): Expression = property(this).greaterThan(float)
    public inline infix fun String.greaterThan(double: Double): Expression = property(this).greaterThan(double)
    public inline infix fun String.greaterThan(boolean: Boolean): Expression = property(this).greaterThan(boolean)
    public inline infix fun String.greaterThan(date: Instant): Expression = property(this).greaterThan(date)

    public inline infix fun String.greaterThanOrEqualTo(expression: Expression): Expression = property(this).greaterThanOrEqualTo(expression)
    public inline infix fun String.greaterThanOrEqualTo(string: String): Expression = property(this).greaterThanOrEqualTo(string)
    public inline infix fun String.greaterThanOrEqualTo(int: Int): Expression = property(this).greaterThanOrEqualTo(int)
    public inline infix fun String.greaterThanOrEqualTo(long: Long): Expression = property(this).greaterThanOrEqualTo(long)
    public inline infix fun String.greaterThanOrEqualTo(float: Float): Expression = property(this).greaterThanOrEqualTo(float)
    public inline infix fun String.greaterThanOrEqualTo(double: Double): Expression = property(this).greaterThanOrEqualTo(double)
    public inline infix fun String.greaterThanOrEqualTo(boolean: Boolean): Expression = property(this).greaterThanOrEqualTo(boolean)
    public inline infix fun String.greaterThanOrEqualTo(date: Instant): Expression = property(this).greaterThanOrEqualTo(date)

    public inline infix fun String.equalTo(expression: Expression): Expression = property(this).equalTo(expression)
    public inline infix fun String.equalTo(string: String): Expression = property(this).equalTo(string)
    public inline infix fun String.equalTo(int: Int): Expression = property(this).equalTo(int)
    public inline infix fun String.equalTo(long: Long): Expression = property(this).equalTo(long)
    public inline infix fun String.equalTo(float: Float): Expression = property(this).equalTo(float)
    public inline infix fun String.equalTo(double: Double): Expression = property(this).equalTo(double)
    public inline infix fun String.equalTo(boolean: Boolean): Expression = property(this).equalTo(boolean)
    public inline infix fun String.equalTo(date: Instant): Expression = property(this).equalTo(date)
    public inline infix fun String.equalTo(map: Map<String, Any?>): Expression = property(this).equalTo(map)
    public inline infix fun String.equalTo(list: List<Any>): Expression = property(this).equalTo(list)

    public inline infix fun String.notEqualTo(expression: Expression): Expression = property(this).notEqualTo(expression)
    public inline infix fun String.notEqualTo(string: String): Expression = property(this).notEqualTo(string)
    public inline infix fun String.notEqualTo(int: Int): Expression = property(this).notEqualTo(int)
    public inline infix fun String.notEqualTo(long: Long): Expression = property(this).notEqualTo(long)
    public inline infix fun String.notEqualTo(float: Float): Expression = property(this).notEqualTo(float)
    public inline infix fun String.notEqualTo(double: Double): Expression = property(this).notEqualTo(double)
    public inline infix fun String.notEqualTo(boolean: Boolean): Expression = property(this).notEqualTo(boolean)
    public inline infix fun String.notEqualTo(date: Instant): Expression = property(this).notEqualTo(date)
    public inline infix fun String.notEqualTo(map: Map<String, Any?>): Expression = property(this).notEqualTo(map)
    public inline infix fun String.notEqualTo(list: List<Any>): Expression = property(this).notEqualTo(list)

    public inline infix fun String.like(expression: Expression): Expression = property(this).like(expression)
    public inline infix fun String.like(string: String): Expression = property(this).like(string)
    public inline infix fun String.like(int: Int): Expression = property(this).like(int)
    public inline infix fun String.like(long: Long): Expression = property(this).like(long)
    public inline infix fun String.like(float: Float): Expression = property(this).like(float)
    public inline infix fun String.like(double: Double): Expression = property(this).like(double)
    public inline infix fun String.like(boolean: Boolean): Expression = property(this).like(boolean)
    public inline infix fun String.like(date: Instant): Expression = property(this).like(date)
}

public class OrderByBuilder {

    private val orderings = mutableListOf<Ordering>()

    public fun String.ascending() {
        orderings += Ordering.property(this).ascending()
    }

    public fun String.descending() {
        orderings += Ordering.property(this).descending()
    }

    public fun Expression.ascending() {
        orderings += Ordering.expression(this).ascending()
    }

    public fun Expression.descending() {
        orderings += Ordering.expression(this).descending()
    }

    public fun orderings(): Array<Ordering> = orderings.toTypedArray()
}
