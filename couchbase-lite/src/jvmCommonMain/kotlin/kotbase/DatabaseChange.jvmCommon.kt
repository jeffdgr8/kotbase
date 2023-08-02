package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.DatabaseChange as CBLDatabaseChange

public actual class DatabaseChange
internal constructor(actual: CBLDatabaseChange) : DelegatedClass<CBLDatabaseChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val documentIDs: List<String>
        get() = actual.documentIDs
}
