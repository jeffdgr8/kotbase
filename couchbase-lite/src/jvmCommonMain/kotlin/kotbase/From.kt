package kotbase

import kotbase.base.DelegatedClass
import kotbase.base.actuals
import com.couchbase.lite.From as CBLFrom

public actual class From
internal constructor(actual: CBLFrom) : DelegatedClass<CBLFrom>(actual), Query by DelegatedQuery(actual) {

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
