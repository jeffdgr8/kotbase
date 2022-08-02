package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class DatabaseConfiguration
internal constructor(actual: com.couchbase.lite.DatabaseConfiguration) :
    DelegatedClass<com.couchbase.lite.DatabaseConfiguration>(actual) {

    public actual constructor(config: DatabaseConfiguration?) : this(
        com.couchbase.lite.DatabaseConfiguration(config?.actual)
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
