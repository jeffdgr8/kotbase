package kotbase

import com.couchbase.lite.Endpoint as CBLEndpoint
import com.couchbase.lite.URLEndpoint as CBLURLEndpoint

public actual interface Endpoint {

    public val actual: CBLEndpoint
}

internal fun CBLEndpoint.asEndpoint(): Endpoint = when (this) {
    is CBLURLEndpoint -> URLEndpoint(this)
    else -> error("Unknown Endpoint type ${this::class}")
}
