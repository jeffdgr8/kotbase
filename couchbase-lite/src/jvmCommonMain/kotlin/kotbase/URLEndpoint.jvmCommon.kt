package kotbase

import kotbase.base.DelegatedClass
import java.net.URI
import com.couchbase.lite.URLEndpoint as CBLURLEndpoint

public actual class URLEndpoint
internal constructor(override val actual: CBLURLEndpoint) : DelegatedClass<CBLURLEndpoint>(actual), Endpoint {

    public actual constructor(url: String) : this(CBLURLEndpoint(URI(url)))

    public actual val url: String
        get() = actual.url.toString()
}
