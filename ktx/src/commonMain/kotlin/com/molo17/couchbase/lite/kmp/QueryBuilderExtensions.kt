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
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package com.molo17.couchbase.lite.kmp

import com.couchbase.lite.kmp.*
import kotlinx.datetime.Instant

inline fun select(vararg expressions: SelectResult) = QueryBuilder.select(*expressions)
inline fun select(vararg keys: String) = QueryBuilder.select(*keys.map(SelectResult::property).toTypedArray())

inline fun all() = SelectResult.all()

inline infix fun Select.from(database: Database) = from(DataSource.database(database))

inline infix fun From.where(builder: WhereBuilder.() -> Expression) = where(WhereBuilder().builder())

inline fun Where.orderBy(builder: OrderByBuilder.() -> Unit) = orderBy(*OrderByBuilder().apply(builder).orderings())

inline fun Where.limit(count: Int, offset: Int? = null) =
    limit(Expression.intValue(count), offset?.let(Expression::intValue))

inline fun OrderBy.limit(count: Int, offset: Int? = null) =
    limit(Expression.intValue(count), offset?.let(Expression::intValue))

inline fun not(expression: Expression) = Expression.not(expression)
inline fun property(name: String) = Expression.property(name)

inline fun Expression.lessThan(string: String) = lessThan(Expression.string(string))
inline fun Expression.lessThan(int: Int) = lessThan(Expression.intValue(int))
inline fun Expression.lessThan(long: Long) = lessThan(Expression.longValue(long))
inline fun Expression.lessThan(float: Float) = lessThan(Expression.floatValue(float))
inline fun Expression.lessThan(double: Double) = lessThan(Expression.doubleValue(double))
inline fun Expression.lessThan(boolean: Boolean) = lessThan(Expression.booleanValue(boolean))
inline fun Expression.lessThan(date: Instant) = lessThan(Expression.date(date))

inline fun Expression.lessThanOrEqualTo(string: String) = lessThanOrEqualTo(Expression.string(string))
inline fun Expression.lessThanOrEqualTo(int: Int) = lessThanOrEqualTo(Expression.intValue(int))
inline fun Expression.lessThanOrEqualTo(long: Long) = lessThanOrEqualTo(Expression.longValue(long))
inline fun Expression.lessThanOrEqualTo(float: Float) = lessThanOrEqualTo(Expression.floatValue(float))
inline fun Expression.lessThanOrEqualTo(double: Double) = lessThanOrEqualTo(Expression.doubleValue(double))
inline fun Expression.lessThanOrEqualTo(boolean: Boolean) = lessThanOrEqualTo(Expression.booleanValue(boolean))
inline fun Expression.lessThanOrEqualTo(date: Instant) = lessThanOrEqualTo(Expression.date(date))

inline fun Expression.greaterThan(string: String) = greaterThan(Expression.string(string))
inline fun Expression.greaterThan(int: Int) = greaterThan(Expression.intValue(int))
inline fun Expression.greaterThan(long: Long) = greaterThan(Expression.longValue(long))
inline fun Expression.greaterThan(float: Float) = greaterThan(Expression.floatValue(float))
inline fun Expression.greaterThan(double: Double) = greaterThan(Expression.doubleValue(double))
inline fun Expression.greaterThan(boolean: Boolean) = greaterThan(Expression.booleanValue(boolean))
inline fun Expression.greaterThan(date: Instant) = greaterThan(Expression.date(date))

inline fun Expression.greaterThanOrEqualTo(string: String) = greaterThanOrEqualTo(Expression.string(string))
inline fun Expression.greaterThanOrEqualTo(int: Int) = greaterThanOrEqualTo(Expression.value(int))
inline fun Expression.greaterThanOrEqualTo(long: Long) = greaterThanOrEqualTo(Expression.longValue(long))
inline fun Expression.greaterThanOrEqualTo(float: Float) = greaterThanOrEqualTo(Expression.floatValue(float))
inline fun Expression.greaterThanOrEqualTo(double: Double) = greaterThanOrEqualTo(Expression.doubleValue(double))
inline fun Expression.greaterThanOrEqualTo(boolean: Boolean) = greaterThanOrEqualTo(Expression.booleanValue(boolean))
inline fun Expression.greaterThanOrEqualTo(date: Instant) = greaterThanOrEqualTo(Expression.date(date))

inline fun Expression.equalTo(string: String) = equalTo(Expression.string(string))
inline fun Expression.equalTo(int: Int) = equalTo(Expression.intValue(int))
inline fun Expression.equalTo(long: Long) = equalTo(Expression.longValue(long))
inline fun Expression.equalTo(float: Float) = equalTo(Expression.floatValue(float))
inline fun Expression.equalTo(double: Double) = equalTo(Expression.doubleValue(double))
inline fun Expression.equalTo(boolean: Boolean) = equalTo(Expression.booleanValue(boolean))
inline fun Expression.equalTo(date: Instant) = equalTo(Expression.date(date))
inline fun Expression.equalTo(map: Map<String, Any?>) = equalTo(Expression.map(map))
inline fun Expression.equalTo(list: List<Any>) = equalTo(Expression.list(list))

inline fun Expression.notEqualTo(string: String) = notEqualTo(Expression.string(string))
inline fun Expression.notEqualTo(int: Int) = notEqualTo(Expression.intValue(int))
inline fun Expression.notEqualTo(long: Long) = notEqualTo(Expression.longValue(long))
inline fun Expression.notEqualTo(float: Float) = notEqualTo(Expression.floatValue(float))
inline fun Expression.notEqualTo(double: Double) = notEqualTo(Expression.doubleValue(double))
inline fun Expression.notEqualTo(boolean: Boolean) = notEqualTo(Expression.booleanValue(boolean))
inline fun Expression.notEqualTo(date: Instant) = notEqualTo(Expression.date(date))
inline fun Expression.notEqualTo(map: Map<String, Any?>) = notEqualTo(Expression.map(map))
inline fun Expression.notEqualTo(list: List<Any>) = notEqualTo(Expression.list(list))

inline fun Expression.like(string: String) = like(Expression.string(string))
inline fun Expression.like(int: Int) = like(Expression.intValue(int))
inline fun Expression.like(long: Long) = like(Expression.longValue(long))
inline fun Expression.like(float: Float) = like(Expression.floatValue(float))
inline fun Expression.like(double: Double) = like(Expression.doubleValue(double))
inline fun Expression.like(boolean: Boolean) = like(Expression.booleanValue(boolean))
inline fun Expression.like(date: Instant) = like(Expression.date(date))

inline infix fun Expression.and(other: Expression) = and(other)
inline infix fun Expression.or(other: Expression) = or(other)

class WhereBuilder {
    inline infix fun String.lessThan(expression: Expression) = property(this).lessThan(expression)
    inline infix fun String.lessThan(string: String) = property(this).lessThan(string)
    inline infix fun String.lessThan(int: Int) = property(this).lessThan(int)
    inline infix fun String.lessThan(long: Long) = property(this).lessThan(long)
    inline infix fun String.lessThan(float: Float) = property(this).lessThan(float)
    inline infix fun String.lessThan(double: Double) = property(this).lessThan(double)
    inline infix fun String.lessThan(boolean: Boolean) = property(this).lessThan(boolean)
    inline infix fun String.lessThan(date: Instant) = property(this).lessThan(date)

    inline infix fun String.lessThanOrEqualTo(expression: Expression) = property(this).lessThanOrEqualTo(expression)
    inline infix fun String.lessThanOrEqualTo(string: String) = property(this).lessThanOrEqualTo(string)
    inline infix fun String.lessThanOrEqualTo(int: Int) = property(this).lessThanOrEqualTo(int)
    inline infix fun String.lessThanOrEqualTo(long: Long) = property(this).lessThanOrEqualTo(long)
    inline infix fun String.lessThanOrEqualTo(float: Float) = property(this).lessThanOrEqualTo(float)
    inline infix fun String.lessThanOrEqualTo(double: Double) = property(this).lessThanOrEqualTo(double)
    inline infix fun String.lessThanOrEqualTo(boolean: Boolean) = property(this).lessThanOrEqualTo(boolean)
    inline infix fun String.lessThanOrEqualTo(date: Instant) = property(this).lessThanOrEqualTo(date)

    inline infix fun String.greaterThan(expression: Expression) = property(this).greaterThan(expression)
    inline infix fun String.greaterThan(string: String) = property(this).greaterThan(string)
    inline infix fun String.greaterThan(int: Int) = property(this).greaterThan(int)
    inline infix fun String.greaterThan(long: Long) = property(this).greaterThan(long)
    inline infix fun String.greaterThan(float: Float) = property(this).greaterThan(float)
    inline infix fun String.greaterThan(double: Double) = property(this).greaterThan(double)
    inline infix fun String.greaterThan(boolean: Boolean) = property(this).greaterThan(boolean)
    inline infix fun String.greaterThan(date: Instant) = property(this).greaterThan(date)

    inline infix fun String.greaterThanOrEqualTo(expression: Expression) = property(this).greaterThanOrEqualTo(expression)
    inline infix fun String.greaterThanOrEqualTo(string: String) = property(this).greaterThanOrEqualTo(string)
    inline infix fun String.greaterThanOrEqualTo(int: Int) = property(this).greaterThanOrEqualTo(int)
    inline infix fun String.greaterThanOrEqualTo(long: Long) = property(this).greaterThanOrEqualTo(long)
    inline infix fun String.greaterThanOrEqualTo(float: Float) = property(this).greaterThanOrEqualTo(float)
    inline infix fun String.greaterThanOrEqualTo(double: Double) = property(this).greaterThanOrEqualTo(double)
    inline infix fun String.greaterThanOrEqualTo(boolean: Boolean) = property(this).greaterThanOrEqualTo(boolean)
    inline infix fun String.greaterThanOrEqualTo(date: Instant) = property(this).greaterThanOrEqualTo(date)

    inline infix fun String.equalTo(expression: Expression) = property(this).equalTo(expression)
    inline infix fun String.equalTo(string: String) = property(this).equalTo(string)
    inline infix fun String.equalTo(int: Int) = property(this).equalTo(int)
    inline infix fun String.equalTo(long: Long) = property(this).equalTo(long)
    inline infix fun String.equalTo(float: Float) = property(this).equalTo(float)
    inline infix fun String.equalTo(double: Double) = property(this).equalTo(double)
    inline infix fun String.equalTo(boolean: Boolean) = property(this).equalTo(boolean)
    inline infix fun String.equalTo(date: Instant) = property(this).equalTo(date)
    inline infix fun String.equalTo(map: Map<String, Any?>) = property(this).equalTo(map)
    inline infix fun String.equalTo(list: List<Any>) = property(this).equalTo(list)

    inline infix fun String.notEqualTo(expression: Expression) = property(this).notEqualTo(expression)
    inline infix fun String.notEqualTo(string: String) = property(this).notEqualTo(string)
    inline infix fun String.notEqualTo(int: Int) = property(this).notEqualTo(int)
    inline infix fun String.notEqualTo(long: Long) = property(this).notEqualTo(long)
    inline infix fun String.notEqualTo(float: Float) = property(this).notEqualTo(float)
    inline infix fun String.notEqualTo(double: Double) = property(this).notEqualTo(double)
    inline infix fun String.notEqualTo(boolean: Boolean) = property(this).notEqualTo(boolean)
    inline infix fun String.notEqualTo(date: Instant) = property(this).notEqualTo(date)
    inline infix fun String.notEqualTo(map: Map<String, Any?>) = property(this).notEqualTo(map)
    inline infix fun String.notEqualTo(list: List<Any>) = property(this).notEqualTo(list)

    inline infix fun String.like(expression: Expression) = property(this).like(expression)
    inline infix fun String.like(string: String) = property(this).like(string)
    inline infix fun String.like(int: Int) = property(this).like(int)
    inline infix fun String.like(long: Long) = property(this).like(long)
    inline infix fun String.like(float: Float) = property(this).like(float)
    inline infix fun String.like(double: Double) = property(this).like(double)
    inline infix fun String.like(boolean: Boolean) = property(this).like(boolean)
    inline infix fun String.like(date: Instant) = property(this).like(date)
}

class OrderByBuilder {

    private val orderings = mutableListOf<Ordering>()

    fun String.ascending() {
        orderings += Ordering.property(this).ascending()
    }
    fun String.descending() {
        orderings += Ordering.property(this).descending()
    }
    fun Expression.ascending() {
        orderings += Ordering.expression(this).ascending()
    }
    fun Expression.descending() {
        orderings += Ordering.expression(this).descending()
    }

    fun orderings() = orderings.toTypedArray()
}
