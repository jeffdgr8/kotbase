package com.couchbase.lite.kmp

public actual class QueryChange
internal constructor(
    public actual val query: Query,
    public actual val results: ResultSet?,
    public actual val error: Throwable?
)
