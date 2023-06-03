package kotbase

import com.couchbase.lite.DatabaseEndpoint
import kotbase.base.DelegatedClass

public actual class DatabaseEndpoint
internal constructor(
    override val actual: com.couchbase.lite.DatabaseEndpoint,
    public actual val database: Database
) : DelegatedClass<DatabaseEndpoint>(actual), Endpoint {

    public actual constructor(database: Database) : this(
        com.couchbase.lite.DatabaseEndpoint(database.actual),
        database
    )
}
