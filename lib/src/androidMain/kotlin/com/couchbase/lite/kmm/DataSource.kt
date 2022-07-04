package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual open class DataSource
private constructor(actual: com.couchbase.lite.DataSource) :
    DelegatedClass<com.couchbase.lite.DataSource>(actual) {

    public actual class As
    internal constructor(override val actual: com.couchbase.lite.DataSource.As) :
        DataSource(actual) {

        public actual fun `as`(alias: String): DataSource = chain {
            actual.`as`(alias)
        }
    }

    public actual companion object {

        public actual fun database(database: Database): As =
            As(com.couchbase.lite.DataSource.database(database.actual))
    }
}
