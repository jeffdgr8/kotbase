package kotbase

/**
 * URL based replication target endpoint
 */
public expect class URLEndpoint

/**
 * Constructor with the url. The supported URL schemes
 * are ws and wss for transferring data over a secure channel.
 *
 * @param url The url.
 */
constructor(url: String) : Endpoint {

    /**
     * Returns the url.
     */
    public val url: String
}
