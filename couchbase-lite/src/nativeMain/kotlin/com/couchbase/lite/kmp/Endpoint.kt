package com.couchbase.lite.kmp

import cnames.structs.CBLEndpoint
import kotlinx.cinterop.CPointer

public actual interface Endpoint {

    public val actual: CPointer<CBLEndpoint>
}
