package com.couchbase.lite.kmp

import cnames.structs.CBLAuthenticator
import kotlinx.cinterop.CPointer

public actual interface Authenticator {

    public val actual: CPointer<CBLAuthenticator>
}
