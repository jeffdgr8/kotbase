package kotbase

import cocoapods.CouchbaseLite.CBLDatabaseChange
import kotbase.base.DelegatedClass

public actual class DatabaseChange
internal constructor(actual: CBLDatabaseChange) : DelegatedClass<CBLDatabaseChange>(actual) {

    public actual val database: Database by lazy {
        Database(actual.database!!)
    }

    @Suppress("UNCHECKED_CAST")
    public actual val documentIDs: List<String>
        get() = actual.documentIDs as List<String>
}
