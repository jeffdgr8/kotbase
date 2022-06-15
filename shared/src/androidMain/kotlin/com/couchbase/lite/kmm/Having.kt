package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.actuals

public actual class Having
internal constructor(actual: com.couchbase.lite.Having) :
    DelegatedClass<com.couchbase.lite.Having>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
