package kotbase

import com.couchbase.lite.GroupBy
import kotbase.base.DelegatedClass
import kotbase.base.actuals

public actual class GroupBy
internal constructor(actual: com.couchbase.lite.GroupBy) :
    DelegatedClass<GroupBy>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun having(expression: Expression): Having =
        Having(actual.having(expression.actual))

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
