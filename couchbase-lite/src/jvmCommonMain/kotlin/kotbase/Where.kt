package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Where as CBLWhere

public actual class Where
internal constructor(actual: CBLWhere) : DelegatedClass<CBLWhere>(actual), Query by DelegatedQuery(actual) {

    public actual fun groupBy(vararg expressions: Expression): GroupBy =
        GroupBy(actual.groupBy(*expressions.actuals()))

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
