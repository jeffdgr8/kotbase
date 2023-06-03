package kotbase

import cocoapods.CouchbaseLite.CBLDatabaseConfiguration
import kotbase.base.DelegatedClass

public actual class DatabaseConfiguration
internal constructor(actual: CBLDatabaseConfiguration) :
    DelegatedClass<CBLDatabaseConfiguration>(actual) {

    public actual constructor(config: DatabaseConfiguration?) : this(
        CBLDatabaseConfiguration(config?.actual)
    )

    public actual fun setDirectory(directory: String): DatabaseConfiguration {
        actual.setDirectory(directory)
        return this
    }

    public actual var directory: String
        get() = actual.directory
        set(value) {
            actual.directory = value
        }
}
