package com.couchbase.lite.kmp

public actual class ValueIndexConfiguration
internal constructor(override val actual: com.couchbase.lite.ValueIndexConfiguration) :
    IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        com.couchbase.lite.ValueIndexConfiguration(*expressions)
    )
}
