package kotbase

import cocoapods.CouchbaseLite.CBLQuery
import cocoapods.CouchbaseLite.CBLQueryLimit
import kotbase.base.AbstractDelegatedClass

public actual class From
internal constructor(private val state: QueryState) : AbstractDelegatedClass<CBLQuery>(), Query by state {

    public actual fun join(vararg joins: Join): Joins {
        return Joins(state.copy(join = joins.actuals()))
    }

    public actual fun where(expression: Expression): Where {
        return Where(state.copy(where = expression.actual))
    }

    public actual fun groupBy(vararg expressions: Expression): GroupBy {
        return GroupBy(state.copy(groupBy = expressions.actuals()))
    }

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        return OrderBy(state.copy(orderBy = orderings.actuals()))
    }

    public actual fun limit(limit: Expression): Limit {
        return Limit(state.copy(limit = CBLQueryLimit.limit(limit.actual)))
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        return Limit(state.copy(limit = CBLQueryLimit.limit(limit.actual, offset?.actual)))
    }

    override val actual: CBLQuery
        get() = state.actual
}
