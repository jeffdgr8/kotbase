package kotbase

public actual interface Endpoint {

    public val actual: com.couchbase.lite.Endpoint
}

internal fun com.couchbase.lite.Endpoint.asEndpoint(): Endpoint {
    return when (this) {
        is com.couchbase.lite.URLEndpoint -> URLEndpoint(this)
        else -> error("Unknown Endpoint type ${this::class}")
    }
}
