package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class Select
internal constructor(actual: com.couchbase.lite.Select) :
    DelegatedClass<com.couchbase.lite.Select>(actual),
    Query by DelegatedQuery(actual) {

    public actual fun from(dataSource: DataSource): From =
        From(actual.from(dataSource.actual))
}
