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
 * Commonly used for `select(Meta.id)`
 * to get a document's ID, or other metadata or expressions.
 *
 * `SELECT Meta().id`
 */
public fun select(expression: Expression, vararg expressions: Expression): Select {
    val results = arrayOf(
        SelectResult.expression(expression),
        *expressions.map(SelectResult::expression).toTypedArray()
    )
    return QueryBuilder.select(*results)
}

/**
 * Commonly used for `select(Meta.id, "foo", "bar")`
 * to get a document's ID along with a set of fields.
 *
 * `SELECT Meta().id, foo, bar`
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
 * Commonly used for `select(Meta.id, all())`
 * to get all of the document, including ID.
 *
 * `SELECT Meta().id, *`
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

public inline infix fun String.from(fromAlias: String): Expression =
    Expression.property(this).from(fromAlias)

public inline infix fun Expression.`as`(alias: String): SelectResult.As =
    SelectResult.expression(this).`as`(alias)

@Suppress("DEPRECATION")
@Deprecated(
    "Use Collection.`as`(String)",
    ReplaceWith("defaultCollection.`as`(alias)")
)
public inline infix fun Database.`as`(alias: String): DataSource =
    DataSource.database(this).`as`(alias)

public inline infix fun Collection.`as`(alias: String): DataSource =
    DataSource.collection(this).`as`(alias)
