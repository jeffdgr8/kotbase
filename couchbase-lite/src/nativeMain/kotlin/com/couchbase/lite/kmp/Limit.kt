package com.couchbase.lite.kmp

public actual class Limit
internal constructor(
    private val state: QueryState,
    internal val limit: Expression,
    internal val offset: Expression? = null
) : Query by state
