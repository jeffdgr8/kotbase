package com.couchbase.lite.kmp

internal class QueryChangeListenerHolder(
    val listener: QueryChangeListener,
    val query: Query
)
