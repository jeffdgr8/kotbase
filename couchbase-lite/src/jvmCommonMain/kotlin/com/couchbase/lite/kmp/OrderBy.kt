package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class OrderBy
internal constructor(actual: com.couchbase.lite.OrderBy) :
    DelegatedClass<com.couchbase.lite.OrderBy>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun limit(limit: Expression): Limit =
        Limit(actual.limit(limit.actual))

    public actual fun limit(limit: Expression, offset: Expression?): Limit =
        Limit(actual.limit(limit.actual, offset?.actual))
}