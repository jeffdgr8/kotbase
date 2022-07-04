package com.couchbase.lite.kmm

import cocoapods.CouchbaseLite.CBLURLEndpoint
import com.udobny.kmm.DelegatedClass
import platform.Foundation.NSURL

public actual class URLEndpoint
internal constructor(override val actual: CBLURLEndpoint) :
    DelegatedClass<CBLURLEndpoint>(actual), Endpoint {

    public actual constructor(url: String) : this(CBLURLEndpoint(NSURL.URLWithString(url)!!))

    public actual val url: String
        get() = actual.url.absoluteString!!
}
