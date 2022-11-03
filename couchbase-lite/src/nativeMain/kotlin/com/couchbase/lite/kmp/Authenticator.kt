package com.couchbase.lite.kmp

import cnames.structs.CBLAuthenticator
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_Free
import kotlin.native.internal.createCleaner

public actual interface Authenticator {

    public val actual: CPointer<CBLAuthenticator>
}
