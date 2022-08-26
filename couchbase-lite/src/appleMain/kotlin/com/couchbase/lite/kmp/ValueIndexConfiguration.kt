package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLValueIndexConfiguration

public actual class ValueIndexConfiguration
internal constructor(override val actual: CBLValueIndexConfiguration) :
    IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        CBLValueIndexConfiguration(expressions.toList())
    )
}
