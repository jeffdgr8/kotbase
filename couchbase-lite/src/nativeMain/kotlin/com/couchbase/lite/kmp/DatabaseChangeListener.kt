package com.couchbase.lite.kmp

internal class DatabaseChangeListenerHolder(
    val listener: DatabaseChangeListener,
    val database: Database
)
