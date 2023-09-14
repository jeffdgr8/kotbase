package kotbase

import java.net.URI
import com.couchbase.lite.URLEndpoint as CBLURLEndpoint

public actual class URLEndpoint
internal constructor(actual: CBLURLEndpoint) : Endpoint(actual) {

    public actual constructor(url: String) : this(CBLURLEndpoint(URI(url)))

    public actual val url: String
        get() = actual.url.toString()
}

internal val URLEndpoint.actual: CBLURLEndpoint
    get() = platformState.actual as CBLURLEndpoint
