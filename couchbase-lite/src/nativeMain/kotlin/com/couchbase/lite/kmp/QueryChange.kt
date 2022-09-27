package com.couchbase.lite.kmp

public actual class QueryChange(
    public actual val query: Query,
    public actual val results: ResultSet?,
    public actual val error: Throwable?
)
