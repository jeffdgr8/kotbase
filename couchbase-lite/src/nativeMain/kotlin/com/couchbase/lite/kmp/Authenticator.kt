package com.couchbase.lite.kmp

import cnames.structs.CBLAuthenticator
import kotlinx.cinterop.CPointer
import libcblite.CBLAuth_Free
import kotlin.native.internal.createCleaner

public actual abstract class Authenticator
internal constructor(internal val actual: CPointer<CBLAuthenticator>) {

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLAuth_Free(it)
    }
}
