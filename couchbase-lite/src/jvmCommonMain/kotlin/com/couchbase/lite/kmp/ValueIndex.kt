package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual class ValueIndex
internal constructor(override val actual: com.couchbase.lite.ValueIndex) :
    DelegatedClass<com.couchbase.lite.ValueIndex>(actual), Index
