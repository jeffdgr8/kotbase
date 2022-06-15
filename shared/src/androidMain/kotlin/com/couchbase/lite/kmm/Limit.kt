package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class Limit
internal constructor(actual: com.couchbase.lite.Limit) :
    DelegatedClass<com.couchbase.lite.Limit>(actual),
    Query by DelegatedQuery(actual)
