package kotbase

import cocoapods.CouchbaseLite.CBLDatabaseEndpoint

public actual class DatabaseEndpoint
internal constructor(
    actual: CBLDatabaseEndpoint,
    public actual val database: Database
) : Endpoint(actual) {

    public actual constructor(database: Database) : this(
        CBLDatabaseEndpoint(database.actual),
        database
    )
}

internal val DatabaseEndpoint.actual: CBLDatabaseEndpoint
    get() = platformState.actual as CBLDatabaseEndpoint
