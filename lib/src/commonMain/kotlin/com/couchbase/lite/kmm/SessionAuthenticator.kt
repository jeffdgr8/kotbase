package com.couchbase.lite.kmm

/**
 * SessionAuthenticator class is an authenticator that will authenticate by using the session ID of
 * the session created by a Sync Gateway
 */
public expect class SessionAuthenticator

/**
 * Initializes with the session ID and the cookie name. If the given cookieName
 * is null, the default cookie name will be used.
 *
 * @param sessionID  Sync Gateway session ID
 * @param cookieName The cookie name
 */
constructor(sessionID: String, cookieName: String? = null) : Authenticator {

    /**
     * Return session ID of the session created by a Sync Gateway.
     */
    public val sessionID: String

    /**
     * Return session cookie name that the session ID value will be set to when communicating
     * the Sync Gateway.
     */
    public val cookieName: String?
}
