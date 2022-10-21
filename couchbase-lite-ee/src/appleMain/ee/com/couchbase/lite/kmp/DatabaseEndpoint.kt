package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLDatabaseEndpoint
import com.udobny.kmp.DelegatedClass

public actual class DatabaseEndpoint
internal constructor(
    override val actual: CBLDatabaseEndpoint,
    public actual val database: Database
) : DelegatedClass<CBLDatabaseEndpoint>(actual), Endpoint {

    public actual constructor(database: Database) : this(
        CBLDatabaseEndpoint(database.actual),
        database
    )
}
