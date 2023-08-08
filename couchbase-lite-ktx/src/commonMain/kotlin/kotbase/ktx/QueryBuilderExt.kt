package kotbase.ktx

import kotbase.*
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
