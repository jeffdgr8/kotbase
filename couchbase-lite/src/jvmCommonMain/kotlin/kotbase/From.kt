package kotbase

import com.couchbase.lite.From
import kotbase.base.DelegatedClass
import kotbase.base.actuals

public actual class From
internal constructor(actual: com.couchbase.lite.From) :
    DelegatedClass<From>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun join(vararg joins: Join): Joins =
        Joins(actual.join(*joins.actuals()))

    public actual fun where(expression: Expression): Where =
        Where(actual.where(expression.actual))

    public actual fun groupBy(vararg expressions: Expression): GroupBy =
        GroupBy(actual.groupBy(*expressions.actuals()))

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
