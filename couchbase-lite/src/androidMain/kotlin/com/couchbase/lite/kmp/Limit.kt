package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class Limit
internal constructor(actual: com.couchbase.lite.Limit) :
    DelegatedClass<com.couchbase.lite.Limit>(actual),
    Query by DelegatedQuery(actual)
