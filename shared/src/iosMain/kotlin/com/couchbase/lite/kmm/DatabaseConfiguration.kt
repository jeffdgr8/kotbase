package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLDatabaseConfiguration
import com.udobny.kmm.DelegatedClass

public actual class DatabaseConfiguration
internal constructor(actual: CBLDatabaseConfiguration) :
    DelegatedClass<CBLDatabaseConfiguration>(actual) {

    public actual constructor(config: DatabaseConfiguration?) : this(
        CBLDatabaseConfiguration(config?.actual)
    )

    public actual fun setDirectory(directory: String): DatabaseConfiguration = chain {
        setDirectory(directory)
    }

    public actual fun getDirectory(): String =
        actual.directory
}
