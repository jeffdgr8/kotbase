package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLIndexConfiguration
import com.udobny.kmm.DelegatedClass

public actual open class IndexConfiguration
internal constructor(override val actual: CBLIndexConfiguration) :
    DelegatedClass<CBLIndexConfiguration>(actual)
