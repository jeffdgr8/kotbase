package kotbase

import com.couchbase.lite.Having
import kotbase.base.DelegatedClass
import kotbase.base.actuals

public actual class Having
internal constructor(actual: com.couchbase.lite.Having) :
    DelegatedClass<Having>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
