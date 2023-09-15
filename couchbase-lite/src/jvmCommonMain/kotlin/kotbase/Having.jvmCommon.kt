package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.Having as CBLHaving

public actual class Having
internal constructor(actual: CBLHaving) : DelegatedClass<CBLHaving>(actual), Query by DelegatedQuery(actual) {

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
