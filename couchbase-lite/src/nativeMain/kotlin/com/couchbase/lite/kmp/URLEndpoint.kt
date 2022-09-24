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

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("unused")
    private val cleaner = createCleaner(actual) {
        CBLEndpoint_Free(it)
    }

    public actual constructor(url: String) : this(
        wrapCBLError { error ->
            CBLEndpoint_CreateWithURL(url.toFLString(), error)!!
        },
        url
    )

    // TODO: throw the right exceptions, either by catching error or validate URL before
//    private companion object {
//
//        private const val SCHEME_STD = "ws"
//        private const val SCHEME_TLS = "wss"
//
//        private fun validate(url: String): NSURL {
//            val nsUrl = NSURL.URLWithString(url)!!
//
//            val scheme = nsUrl.scheme
//            if (!((SCHEME_STD == scheme) || (SCHEME_TLS == scheme))) {
//                throw IllegalArgumentException("Invalid scheme for URLEndpoint url ($url). It must be either 'ws:' or 'wss:'.")
//            }
//
//            if (nsUrl.password != null) {
//                throw IllegalArgumentException("Embedded credentials in a URL (username:password@url) are not allowed. Use the BasicAuthenticator class instead.")
//            }
//
//            return nsUrl
//        }
//    }
}
