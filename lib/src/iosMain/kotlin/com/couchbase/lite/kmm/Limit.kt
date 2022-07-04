package com.couchbase.lite.kmm

public actual class Limit
internal constructor(private val state: QueryState) : Query by state
