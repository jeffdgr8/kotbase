package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class DatabaseEndpoint
internal constructor(
    override val actual: com.couchbase.lite.DatabaseEndpoint,
    public actual val database: Database
) : DelegatedClass<com.couchbase.lite.DatabaseEndpoint>(actual), Endpoint {

    public actual constructor(database: Database) : this(
        com.couchbase.lite.DatabaseEndpoint(database.actual),
        database
    )
}
