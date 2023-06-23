package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.DatabaseEndpoint as CBLDatabaseEndpoint

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
