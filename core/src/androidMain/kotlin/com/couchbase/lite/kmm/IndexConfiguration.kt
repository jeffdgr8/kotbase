package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual open class IndexConfiguration
internal constructor(override val actual: com.couchbase.lite.IndexConfiguration) :
    DelegatedClass<com.couchbase.lite.IndexConfiguration>(actual) {

    public actual val expressions: List<String>
        get() = actual.expressions
}
