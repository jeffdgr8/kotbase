package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLValueIndex
import com.udobny.kmm.DelegatedClass

public actual class ValueIndex
internal constructor(override val actual: CBLValueIndex = CBLValueIndex()) :
    DelegatedClass<CBLValueIndex>(actual), Index
