package kotbase

import com.couchbase.lite.URLEndpoint
import kotbase.base.DelegatedClass
import java.net.URI

public actual class URLEndpoint
internal constructor(override val actual: com.couchbase.lite.URLEndpoint) :
    DelegatedClass<URLEndpoint>(actual), Endpoint {

    public actual constructor(url: String) : this(com.couchbase.lite.URLEndpoint(URI(url)))

    public actual val url: String
        get() = actual.url.toString()
}
