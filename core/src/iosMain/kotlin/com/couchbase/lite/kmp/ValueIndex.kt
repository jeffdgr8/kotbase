package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLValueIndex
import com.udobny.kmp.DelegatedClass

public actual class ValueIndex
internal constructor(override val actual: CBLValueIndex = CBLValueIndex()) :
    DelegatedClass<CBLValueIndex>(actual), Index
