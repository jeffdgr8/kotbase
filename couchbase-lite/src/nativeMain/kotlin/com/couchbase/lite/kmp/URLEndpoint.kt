package com.couchbase.lite.kmp

import cnames.structs.CBLEndpoint
import com.couchbase.lite.kmp.internal.fleece.toFLString
import com.couchbase.lite.kmp.internal.wrapCBLError
import kotlinx.cinterop.CPointer
import libcblite.CBLEndpoint_CreateWithURL
import libcblite.CBLEndpoint_Free
import kotlin.native.internal.createCleaner

public actual class URLEndpoint
internal constructor(
    override val actual: CPointer<CBLEndpoint>,
    public actual val url: String
) : Endpoint {

    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLEndpoint_Free(it)
    }

    public actual constructor(url: String) : this(
        try {
            wrapCBLError { error ->
                CBLEndpoint_CreateWithURL(url.toFLString(), error)!!
            }
        } catch (e: CouchbaseLiteException) {
            throw IllegalArgumentException(e.message, e)
        },
        url
    )
}
