package com.couchbase.lite.kmp

import com.udobny.kmp.DelegatedClass

public actual open class IndexConfiguration
internal constructor(override val actual: com.couchbase.lite.IndexConfiguration) :
    DelegatedClass<com.couchbase.lite.IndexConfiguration>(actual) {

    public actual val expressions: List<String>
        get() = actual.expressions
}
