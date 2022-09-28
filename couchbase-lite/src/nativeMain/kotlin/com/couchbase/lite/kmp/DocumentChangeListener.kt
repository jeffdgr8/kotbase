package com.couchbase.lite.kmp

internal class DocumentChangeListenerHolder(
    val listener: DocumentChangeListener,
    val database: Database
)
