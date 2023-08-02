package kotbase

import kotbase.base.DelegatedClass
import com.couchbase.lite.DatabaseConfiguration as CBLDatabaseConfiguration

public actual class DatabaseConfiguration
internal constructor(actual: CBLDatabaseConfiguration) : DelegatedClass<CBLDatabaseConfiguration>(actual) {

    public actual constructor(config: DatabaseConfiguration?) : this(CBLDatabaseConfiguration(config?.actual))

    public actual fun setDirectory(directory: String): DatabaseConfiguration {
        actual.directory = directory
        return this
    }

    public actual var directory: String
        get() = actual.directory
        set(value) {
            actual.directory = value
        }

    private companion object {

        init {
            CouchbaseLite.internalInit()
        }
    }
}
