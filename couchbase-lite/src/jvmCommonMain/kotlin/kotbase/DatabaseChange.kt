package kotbase

import com.couchbase.lite.DatabaseChange
import kotbase.base.DelegatedClass

public actual class DatabaseChange
internal constructor(actual: com.couchbase.lite.DatabaseChange) :
    DelegatedClass<DatabaseChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database)
    }

    public actual val documentIDs: List<String>
        get() = actual.documentIDs
}
