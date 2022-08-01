package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDatabaseConfiguration
import com.udobny.kmm.DelegatedClass

public actual class DatabaseConfiguration
internal constructor(actual: CBLDatabaseConfiguration) :
    DelegatedClass<CBLDatabaseConfiguration>(actual) {

    public actual constructor(config: DatabaseConfiguration?) : this(
        CBLDatabaseConfiguration(config?.actual)
    )

    public constructor() : this(null)

    public actual fun setDirectory(directory: String): DatabaseConfiguration = chain {
        setDirectory(directory)
    }

    public actual var directory: String
        get() = actual.directory
        set(value) {
            actual.directory = value
        }
}
