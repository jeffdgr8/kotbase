package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual class ValueIndex
internal constructor(override val actual: com.couchbase.lite.ValueIndex) :
    DelegatedClass<com.couchbase.lite.ValueIndex>(actual), Index
