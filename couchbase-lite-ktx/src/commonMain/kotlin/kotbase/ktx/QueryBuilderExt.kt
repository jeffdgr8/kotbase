package kotbase.ktx

import kotbase.*
import kotbase.Function

/**
 * Commonly used for `select(Meta.id)`
 * to get only a document's ID.
 *
 * `SELECT Meta().id`
 */
public inline fun select(expression: Expression): Select =
    QueryBuilder.select(SelectResult.expression(expression))

/**
 * Commonly used for `select(Meta.id, "foo", "bar")`
 * to get a document's ID along with a set of fields.
 *
 * `SELECT Meta().id, foo, bar`
 */
public inline fun select(expression: Expression, vararg properties: String): Select {
    val results = buildList {
        add(SelectResult.expression(expression))
        addAll(properties.map(SelectResult::property))
    }.toTypedArray()
    return QueryBuilder.select(*results)
}

/**
 * Commonly used for `select(Meta.id, all())`
 * to get all of the document, including ID.
 *
 * `SELECT Meta().id, *`
 */
public inline fun select(expression: Expression, vararg results: SelectResult): Select {
    val allResults = buildList {
        add(SelectResult.expression(expression))
        addAll(results)
    }.toTypedArray()
    return QueryBuilder.select(*allResults)
}

/**
 * `SELECT COUNT(*)`
 *
 * @see countResult
 */
public inline fun selectCount(): Select =
    QueryBuilder.select(SelectResult.expression(Function.count(Expression.string("*"))))