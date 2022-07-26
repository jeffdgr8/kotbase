package com.couchbase.lite.kmm

import com.couchbase.lite.FullTextIndexConfiguration
import com.couchbase.lite.ValueIndexConfiguration
import com.couchbase.lite.expressions
import com.udobny.kmm.DelegatedClass

public actual open class IndexConfiguration
// TODO: actual should really be com.couchbase.lite.IndexConfiguration, but is not visible
//  https://forums.couchbase.com/t/can-indexconfiguration-be-made-public/33772
internal constructor(override val actual: com.couchbase.lite.AbstractIndex) :
    DelegatedClass<com.couchbase.lite.AbstractIndex>(actual) {

    public actual val expressions: List<String>
        get() = when (val actual = actual) {
            is FullTextIndexConfiguration -> actual.expressions
            is ValueIndexConfiguration -> actual.expressions
            else -> error("Unknown IndexConfiguration ${actual::class}")
        }
}
