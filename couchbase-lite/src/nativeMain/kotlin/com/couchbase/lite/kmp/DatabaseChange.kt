package com.couchbase.lite.kmp

public actual class DatabaseChange(
    public actual val database: Database,
    public actual val documentIDs: List<String>
)
