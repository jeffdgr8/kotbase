package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLValueIndexConfiguration

// TODO: https://forums.couchbase.com/t/cblvalueindexconfiguration-and-cblfulltextindexconfiguration-missing-from-objc-framework-for-x86-64/33815
public actual class ValueIndexConfiguration
internal constructor(override val actual: CBLValueIndexConfiguration) :
    IndexConfiguration(actual) {

    public actual constructor(vararg expressions: String) : this(
        CBLValueIndexConfiguration(expressions.toList())
    )
}
