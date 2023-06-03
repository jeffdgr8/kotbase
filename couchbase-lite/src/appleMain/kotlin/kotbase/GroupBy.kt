package kotbase

import cocoapods.CouchbaseLite.CBLQueryLimit
import kotbase.base.actuals

public actual class GroupBy
internal constructor(private val state: QueryState) : Query by state {

    public actual fun having(expression: Expression): Having {
        state.having = expression.actual
        return Having(state)
    }

    public actual fun orderBy(vararg orderings: Ordering): OrderBy {
        state.orderBy = orderings.actuals()
        return OrderBy(state)
    }

    public actual fun limit(limit: Expression): Limit {
        state.limit = CBLQueryLimit.limit(limit.actual)
        return Limit(state)
    }

    public actual fun limit(limit: Expression, offset: Expression?): Limit {
        state.limit = CBLQueryLimit.limit(limit.actual, offset?.actual)
        return Limit(state)
    }
}
