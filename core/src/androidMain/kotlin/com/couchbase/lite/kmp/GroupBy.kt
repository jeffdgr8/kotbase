package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass
import com.udobny.kmp.actuals

public actual class GroupBy
internal constructor(actual: com.couchbase.lite.GroupBy) :
    DelegatedClass<com.couchbase.lite.GroupBy>(actual),
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
