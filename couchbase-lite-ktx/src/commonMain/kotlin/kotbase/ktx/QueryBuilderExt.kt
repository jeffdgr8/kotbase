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

import kotbase.*
import kotbase.Collection
import kotbase.Function

/**
 * Create a SELECT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * Commonly used for `select(Meta.id)`
 * to get a document's ID, or other metadata or expressions.
 *
 * `SELECT Meta().id`
 *
 * @param expressions The expressions for specifying the returned values.
 * @return A Select object.
 */
public fun select(expression: Expression, vararg expressions: Expression): Select {
    val results = arrayOf(
        SelectResult.expression(expression),
        *expressions.map(SelectResult::expression).toTypedArray()
    )
    return QueryBuilder.select(*results)
}

/**
 * Create a SELECT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * Commonly used for `select(Meta.id, "foo", "bar")`
 * to get a document's ID along with a set of fields.
 *
 * `SELECT Meta().id, foo, bar`
 *
 * @param expression An expression for specifying the returned value.
 * @param properties Properties for specifying the returned values.
 * @return A Select object.
 */
public fun select(expression: Expression, property: String, vararg properties: String): Select {
    val results = arrayOf(
        SelectResult.expression(expression),
        SelectResult.property(property),
        *properties.map(SelectResult::property).toTypedArray()
    )
    return QueryBuilder.select(*results)
}

/**
 * Create a SELECT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * Commonly used for `select(Meta.id, all())`
 * to get all of the document, including ID.
 *
 * `SELECT Meta().id, *`
 *
 * @param expression An expression for specifying the returned value.
 * @param results The SelectResult objects for specifying the returned values.
 * @return A Select object.
 */
public fun select(expression: Expression, result: SelectResult, vararg results: SelectResult): Select {
    val allResults = arrayOf(
        SelectResult.expression(expression),
        result,
        *results
    )
    return QueryBuilder.select(*allResults)
}

/**
 * Create a SELECT DISTINCT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param results The array of the SelectResult object for specifying the returned values.
 * @return A Select distinct object.
 */
public inline fun selectDistinct(vararg results: SelectResult): Select =
    QueryBuilder.selectDistinct(*results)

/**
 * Create a SELECT DISTINCT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param properties The properties for specifying the returned values.
 * @return A Select distinct object.
 */
public inline fun selectDistinct(vararg properties: String): Select =
    QueryBuilder.selectDistinct(*properties.map(SelectResult::property).toTypedArray())

/**
 * Create a SELECT DISTINCT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param expressions The expressions for specifying the returned values.
 * @return A Select distinct object.
 */
public fun selectDistinct(expression: Expression, vararg expressions: Expression): Select {
    val results = arrayOf(
        SelectResult.expression(expression),
        *expressions.map(SelectResult::expression).toTypedArray()
    )
    return QueryBuilder.selectDistinct(*results)
}

/**
 * Create a SELECT DISTINCT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param expression An expression for specifying the returned value.
 * @param properties Properties for specifying the returned values.
 * @return A Select distinct object.
 */
public fun selectDistinct(expression: Expression, property: String, vararg properties: String): Select {
    val results = arrayOf(
        SelectResult.expression(expression),
        SelectResult.property(property),
        *properties.map(SelectResult::property).toTypedArray()
    )
    return QueryBuilder.selectDistinct(*results)
}

/**
 * Create a SELECT DISTINCT statement instance that you can use further
 * (e.g. calling the from() function) to construct the complete query statement.
 *
 * @param expression An expression for specifying the returned value.
 * @param results The SelectResult objects for specifying the returned values.
 * @return A Select distinct object.
 */
public fun selectDistinct(expression: Expression, result: SelectResult, vararg results: SelectResult): Select {
    val allResults = arrayOf(
        SelectResult.expression(expression),
        result,
        *results
    )
    return QueryBuilder.selectDistinct(*allResults)
}

/**
 * `SELECT COUNT(*)`
 *
 * @param alias optional alias for count
 *
 * @see countResult
 */
public fun selectCount(alias: String = ""): Select {
    val selectResult = SelectResult.expression(Function.count(Expression.string("*")))
    val selectAs = if (alias.isNotBlank()) {
        selectResult.`as`(alias)
    } else {
        selectResult
    }
    return QueryBuilder.select(selectAs)
}

/**
 * Specifies an alias name of the data source to query the property data from.
 *
 * @param fromAlias The alias name of the data source.
 * @return The property Expression with the given data source alias name.
 */
public inline infix fun String.from(fromAlias: String): Expression =
    Expression.property(this).from(fromAlias)

/**
 * Specifies the alias for the Expression as a SelectResult object.
 *
 * @param alias The alias name.
 * @return The SelectResult object with the alias name specified.
 */
public inline infix fun Expression.`as`(alias: String): SelectResult.As =
    SelectResult.expression(this).`as`(alias)

/**
 * Set an alias to the database as a data source.
 *
 * @param alias the alias to set.
 * @return the data source object with the given alias set.
 */
@Suppress("DEPRECATION")
@Deprecated(
    "Use Collection.`as`(String)",
    ReplaceWith("defaultCollection.`as`(alias)")
)
public inline infix fun Database.`as`(alias: String): DataSource =
    DataSource.database(this).`as`(alias)

/**
 * Set an alias to the collection as a data source.
 *
 * @param alias the alias to set.
 * @return the data source object with the given alias set.
 */
public inline infix fun Collection.`as`(alias: String): DataSource =
    DataSource.collection(this).`as`(alias)

/**
 * Create and chain a GroupBy object to group the query result.
 *
 * @param properties The properties to group by.
 * @return The GroupBy object that represents the GROUP BY clause of the query.
 */
public fun GroupByRouter.groupBy(vararg properties: String): GroupBy =
    groupBy(*properties.map(Expression::property).toTypedArray())
