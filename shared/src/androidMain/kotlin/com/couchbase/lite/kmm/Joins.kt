package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import com.udobny.kmm.actuals

public actual class Joins
internal constructor(actual: com.couchbase.lite.Joins) :
    DelegatedClass<com.couchbase.lite.Joins>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun where(expression: Expression): Where =
        Where(actual.where(expression.actual))

    public actual fun orderBy(vararg orderings: Ordering): OrderBy =
        OrderBy(actual.orderBy(*orderings.actuals()))

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}
