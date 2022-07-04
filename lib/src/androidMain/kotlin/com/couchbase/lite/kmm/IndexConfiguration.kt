package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass

public actual open class IndexConfiguration
// TODO: actual should really be com.couchbase.lite.IndexConfiguration, but is not visible
//  https://forums.couchbase.com/t/can-indexconfiguration-be-made-public/33772
internal constructor(override val actual: com.couchbase.lite.AbstractIndex) :
    DelegatedClass<com.couchbase.lite.AbstractIndex>(actual)
