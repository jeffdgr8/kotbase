package kotbase

import cocoapods.CouchbaseLite.CBLQuery
import cocoapods.CouchbaseLite.CBLQueryLimit
import kotbase.base.AbstractDelegatedClass
import kotbase.base.actuals

public actual class Having
internal constructor(private val state: QueryState) : AbstractDelegatedClass<CBLQuery>(), Query by state {

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