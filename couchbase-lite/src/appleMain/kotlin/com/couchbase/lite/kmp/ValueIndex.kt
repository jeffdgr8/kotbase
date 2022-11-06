package com.couchbase.lite.kmp

import cocoapods.CouchbaseLite.CBLValueIndex

public actual class ValueIndex
internal constructor(override val actual: CBLValueIndex = CBLValueIndex()) : Index(actual)
