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
 * From https://github.com/MOLO17/couchbase-lite-kotlin/blob/master/library/src/main/java/com/molo17/couchbase/lite/QueryBuilderExtensions.kt
 * Modified by Jeff Lockhart
 * - Use kotbase package
 * - Resolve explicitApiWarning() requirements
 * - Use router interfaces to add support for all Query subclasses
 * - Add from Collection DataSource API
 * - Make functions infix
 * - Add docs
 */

@file:Suppress("NOTHING_TO_INLINE", "unused", "KotlinRedundantDiagnosticSuppress")

package kotbase.ktx

import kotbase.*
import kotbase.Collection
import kotlinx.datetime.Instant
import kotlin.Array

/**
 * Create a SELECT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param results The SelectResult objects for specifying the returned values.
 * @return A Select object.
 */
public inline fun select(vararg results: SelectResult): Select = QueryBuilder.select(*results)

/**
 * Create a SELECT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param properties The properties for specifying the returned values.
 * @return A Select object.
 */
public inline fun select(vararg properties: String): Select = QueryBuilder.select(*properties.map(SelectResult::property).toTypedArray())

/**
 * Creates a SelectResult that contains values for all properties matching the query.
 * The result is a single CBLMutableDictionary whose key is the name of the data source.
 *
 * @return a SelectResult.From that can be used to alias the property.
 */
public inline fun all(): SelectResult.From = SelectResult.all()

/**
 * Create and chain a FROM component for specifying the query's data source.
 *
 * @param database the database source.
 * @return the From component.
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use from(Collection)",
    ReplaceWith("from(database.defaultCollection)")
)
public inline infix fun FromRouter.from(database: Database): From = from(DataSource.database(database))

/**
 * Create and chain a FROM component for specifying the query's data source.
 *
 * @param collection the collection source.
 * @return the From component.
 */
public inline infix fun FromRouter.from(collection: Collection): From = from(DataSource.collection(collection))

/**
 * Create and chain a WHERE component for specifying the WHERE clause of the query.
 *
 * @param builder a WhereBuilder block specifying the WHERE clause expression.
 * @return the WHERE component.
 */
public inline infix fun WhereRouter.where(builder: WhereBuilder.() -> Expression): Where = where(WhereBuilder().builder())

/**
 * Create and chain an ORDER BY component for specifying the ORDER BY clause of the query.
 *
 * @param builder an OrderByBuilder block specifying properties and expressions to ORDER BY.
 * @return the ORDER BY component.
 */
public infix fun OrderByRouter.orderBy(builder: OrderByBuilder.() -> Unit): OrderBy = orderBy(*OrderByBuilder().apply(builder).orderings())

/**
 * Creates and chains a Limit object to skip the returned results for the given offset
 * position and to limit the number of results to not more than the given limit value.
 *
 * @param limit  The limit value.
 * @param offset The offset value.
 * @return The Limit object that represents the LIMIT clause of the query.
 */
public inline fun LimitRouter.limit(limit: Int, offset: Int? = null): Limit = limit(Expression.intValue(limit), offset?.let(Expression::intValue))

/**
 * Create a negated expression to represent the negated result of the given expression.
 *
 * @param expression the expression to be negated.
 * @return a negated expression.
 */
public inline fun not(expression: Expression): Expression = Expression.not(expression)

/**
 * Create a property expression representing the value of the given property.
 *
 * @param property the name of the property in the form of a key path.
 * @return a property expression.
 */
public inline fun property(property: String): PropertyExpression = Expression.property(property)

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(string: String): Expression = lessThan(Expression.string(string))

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(int: Int): Expression = lessThan(Expression.intValue(int))

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(long: Long): Expression = lessThan(Expression.longValue(long))

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(float: Float): Expression = lessThan(Expression.floatValue(float))

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(double: Double): Expression = lessThan(Expression.doubleValue(double))

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(boolean: Boolean): Expression = lessThan(Expression.booleanValue(boolean))

/**
 * Create a less than expression that evaluates whether or not the current expression
 * is less than the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return a less than expression.
 */
public inline infix fun Expression.lessThan(date: Instant): Expression = lessThan(Expression.date(date))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(string: String): Expression = lessThanOrEqualTo(Expression.string(string))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(int: Int): Expression = lessThanOrEqualTo(Expression.intValue(int))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(long: Long): Expression = lessThanOrEqualTo(Expression.longValue(long))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(float: Float): Expression = lessThanOrEqualTo(Expression.floatValue(float))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(double: Double): Expression = lessThanOrEqualTo(Expression.doubleValue(double))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(boolean: Boolean): Expression = lessThanOrEqualTo(Expression.booleanValue(boolean))

/**
 * Create a less than or equal to expression that evaluates whether or not the current
 * expression is less than or equal to the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return a less than or equal to expression.
 */
public inline infix fun Expression.lessThanOrEqualTo(date: Instant): Expression = lessThanOrEqualTo(Expression.date(date))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(string: String): Expression = greaterThan(Expression.string(string))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(int: Int): Expression = greaterThan(Expression.intValue(int))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(long: Long): Expression = greaterThan(Expression.longValue(long))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(float: Float): Expression = greaterThan(Expression.floatValue(float))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(double: Double): Expression = greaterThan(Expression.doubleValue(double))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(boolean: Boolean): Expression = greaterThan(Expression.booleanValue(boolean))

/**
 * Create a greater than expression that evaluates whether or not the current expression
 * is greater than the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return a greater than expression.
 */
public inline infix fun Expression.greaterThan(date: Instant): Expression = greaterThan(Expression.date(date))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(string: String): Expression = greaterThanOrEqualTo(Expression.string(string))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(int: Int): Expression = greaterThanOrEqualTo(Expression.value(int))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(long: Long): Expression = greaterThanOrEqualTo(Expression.longValue(long))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(float: Float): Expression = greaterThanOrEqualTo(Expression.floatValue(float))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(double: Double): Expression = greaterThanOrEqualTo(Expression.doubleValue(double))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(boolean: Boolean): Expression = greaterThanOrEqualTo(Expression.booleanValue(boolean))

/**
 * Create a greater than or equal to expression that evaluates whether or not the current
 * expression is greater than or equal to the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return a greater than or equal to expression.
 */
public inline infix fun Expression.greaterThanOrEqualTo(date: Instant): Expression = greaterThanOrEqualTo(Expression.date(date))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(string: String): Expression = equalTo(Expression.string(string))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(int: Int): Expression = equalTo(Expression.intValue(int))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(long: Long): Expression = equalTo(Expression.longValue(long))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(float: Float): Expression = equalTo(Expression.floatValue(float))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(double: Double): Expression = equalTo(Expression.doubleValue(double))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(boolean: Boolean): Expression = equalTo(Expression.booleanValue(boolean))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(date: Instant): Expression = equalTo(Expression.date(date))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param map the map expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(map: Map<String, Any?>): Expression = equalTo(Expression.map(map))

/**
 * Create an equal to expression that evaluates whether or not the current expression
 * is equal to the given expression.
 *
 * @param list the list expression to compare with the current expression.
 * @return an equal to expression.
 */
public inline infix fun Expression.equalTo(list: List<Any>): Expression = equalTo(Expression.list(list))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(string: String): Expression = notEqualTo(Expression.string(string))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(int: Int): Expression = notEqualTo(Expression.intValue(int))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(long: Long): Expression = notEqualTo(Expression.longValue(long))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(float: Float): Expression = notEqualTo(Expression.floatValue(float))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(double: Double): Expression = notEqualTo(Expression.doubleValue(double))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(boolean: Boolean): Expression = notEqualTo(Expression.booleanValue(boolean))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(date: Instant): Expression = notEqualTo(Expression.date(date))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param map the map expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(map: Map<String, Any?>): Expression = notEqualTo(Expression.map(map))

/**
 * Create a NOT equal to expression that evaluates whether or not the current expression
 * is not equal to the given expression.
 *
 * @param list the list expression to compare with the current expression.
 * @return a NOT equal to expression.
 */
public inline infix fun Expression.notEqualTo(list: List<Any>): Expression = notEqualTo(Expression.list(list))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param string the string expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(string: String): Expression = like(Expression.string(string))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param int the int expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(int: Int): Expression = like(Expression.intValue(int))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param long the long expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(long: Long): Expression = like(Expression.longValue(long))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param float the float expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(float: Float): Expression = like(Expression.floatValue(float))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param double the double expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(double: Double): Expression = like(Expression.doubleValue(double))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param boolean the boolean expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(boolean: Boolean): Expression = like(Expression.booleanValue(boolean))

/**
 * Create a Like expression that evaluates whether or not the current expression is LIKE
 * the given expression.
 *
 * @param date the date expression to compare with the current expression.
 * @return a Like expression.
 */
public inline infix fun Expression.like(date: Instant): Expression = like(Expression.date(date))

public class WhereBuilder {

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(expression: Expression): Expression = property(this).lessThan(expression)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(string: String): Expression = property(this).lessThan(string)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(int: Int): Expression = property(this).lessThan(int)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(long: Long): Expression = property(this).lessThan(long)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(float: Float): Expression = property(this).lessThan(float)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(double: Double): Expression = property(this).lessThan(double)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(boolean: Boolean): Expression = property(this).lessThan(boolean)

    /**
     * Create a less than expression that evaluates whether or not the current property
     * is less than the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return a less than expression.
     */
    public inline infix fun String.lessThan(date: Instant): Expression = property(this).lessThan(date)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(expression: Expression): Expression = property(this).lessThanOrEqualTo(expression)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(string: String): Expression = property(this).lessThanOrEqualTo(string)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(int: Int): Expression = property(this).lessThanOrEqualTo(int)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(long: Long): Expression = property(this).lessThanOrEqualTo(long)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(float: Float): Expression = property(this).lessThanOrEqualTo(float)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(double: Double): Expression = property(this).lessThanOrEqualTo(double)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(boolean: Boolean): Expression = property(this).lessThanOrEqualTo(boolean)

    /**
     * Create a less than or equal to expression that evaluates whether or not the current
     * property is less than or equal to the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return a less than or equal to expression.
     */
    public inline infix fun String.lessThanOrEqualTo(date: Instant): Expression = property(this).lessThanOrEqualTo(date)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(expression: Expression): Expression = property(this).greaterThan(expression)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(string: String): Expression = property(this).greaterThan(string)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(int: Int): Expression = property(this).greaterThan(int)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(long: Long): Expression = property(this).greaterThan(long)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(float: Float): Expression = property(this).greaterThan(float)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(double: Double): Expression = property(this).greaterThan(double)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(boolean: Boolean): Expression = property(this).greaterThan(boolean)

    /**
     * Create a greater than expression that evaluates whether or not the current property
     * is greater than the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return a greater than expression.
     */
    public inline infix fun String.greaterThan(date: Instant): Expression = property(this).greaterThan(date)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(expression: Expression): Expression = property(this).greaterThanOrEqualTo(expression)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(string: String): Expression = property(this).greaterThanOrEqualTo(string)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(int: Int): Expression = property(this).greaterThanOrEqualTo(int)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(long: Long): Expression = property(this).greaterThanOrEqualTo(long)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(float: Float): Expression = property(this).greaterThanOrEqualTo(float)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(double: Double): Expression = property(this).greaterThanOrEqualTo(double)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(boolean: Boolean): Expression = property(this).greaterThanOrEqualTo(boolean)

    /**
     * Create a greater than or equal to expression that evaluates whether or not the current
     * property is greater than or equal to the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return a greater than or equal to expression.
     */
    public inline infix fun String.greaterThanOrEqualTo(date: Instant): Expression = property(this).greaterThanOrEqualTo(date)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(expression: Expression): Expression = property(this).equalTo(expression)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(string: String): Expression = property(this).equalTo(string)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(int: Int): Expression = property(this).equalTo(int)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(long: Long): Expression = property(this).equalTo(long)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(float: Float): Expression = property(this).equalTo(float)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(double: Double): Expression = property(this).equalTo(double)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(boolean: Boolean): Expression = property(this).equalTo(boolean)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(date: Instant): Expression = property(this).equalTo(date)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param map the map expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(map: Map<String, Any?>): Expression = property(this).equalTo(map)

    /**
     * Create an equal to expression that evaluates whether or not the current property
     * is equal to the given expression.
     *
     * @param list the list expression to compare with the current expression.
     * @return an equal to expression.
     */
    public inline infix fun String.equalTo(list: List<Any>): Expression = property(this).equalTo(list)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(expression: Expression): Expression = property(this).notEqualTo(expression)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(string: String): Expression = property(this).notEqualTo(string)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(int: Int): Expression = property(this).notEqualTo(int)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(long: Long): Expression = property(this).notEqualTo(long)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(float: Float): Expression = property(this).notEqualTo(float)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(double: Double): Expression = property(this).notEqualTo(double)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(boolean: Boolean): Expression = property(this).notEqualTo(boolean)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(date: Instant): Expression = property(this).notEqualTo(date)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param map the map expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(map: Map<String, Any?>): Expression = property(this).notEqualTo(map)

    /**
     * Create a NOT equal to expression that evaluates whether or not the current property
     * is not equal to the given expression.
     *
     * @param list the list expression to compare with the current expression.
     * @return a NOT equal to expression.
     */
    public inline infix fun String.notEqualTo(list: List<Any>): Expression = property(this).notEqualTo(list)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param expression the expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(expression: Expression): Expression = property(this).like(expression)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param string the string expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(string: String): Expression = property(this).like(string)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param int the int expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(int: Int): Expression = property(this).like(int)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param long the long expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(long: Long): Expression = property(this).like(long)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param float the float expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(float: Float): Expression = property(this).like(float)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param double the double expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(double: Double): Expression = property(this).like(double)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param boolean the boolean expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(boolean: Boolean): Expression = property(this).like(boolean)

    /**
     * Create a Like expression that evaluates whether or not the current property is LIKE
     * the given expression.
     *
     * @param date the date expression to compare with the current expression.
     * @return a Like expression.
     */
    public inline infix fun String.like(date: Instant): Expression = property(this).like(date)
}

public class OrderByBuilder {

    private val orderings = mutableListOf<Ordering>()

    /**
     * Sort by the given property name in ascending order.
     */
    public fun String.ascending() {
        orderings += Ordering.property(this).ascending()
    }

    /**
     * Sort by the given property name in descending order.
     */
    public fun String.descending() {
        orderings += Ordering.property(this).descending()
    }

    /**
     * Sort by the given expression in ascending order.
     */
    public fun Expression.ascending() {
        orderings += Ordering.expression(this).ascending()
    }

    /**
     * Sort by the given expression in descending order.
     */
    public fun Expression.descending() {
        orderings += Ordering.expression(this).descending()
    }

    internal fun orderings(): Array<Ordering> = orderings.toTypedArray()
}
