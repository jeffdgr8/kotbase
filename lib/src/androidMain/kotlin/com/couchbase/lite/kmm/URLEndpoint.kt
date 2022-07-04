package com.couchbase.lite.kmm

import com.udobny.kmm.DelegatedClass
import java.net.URI

public actual class URLEndpoint
internal constructor(override val actual: com.couchbase.lite.URLEndpoint) :
    DelegatedClass<com.couchbase.lite.URLEndpoint>(actual), Endpoint {

    public actual constructor(url: String) : this(com.couchbase.lite.URLEndpoint(URI(url)))

    public actual val url: String
        get() = actual.url.toString()
}
