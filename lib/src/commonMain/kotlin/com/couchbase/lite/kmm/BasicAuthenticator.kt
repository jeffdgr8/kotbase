package com.couchbase.lite.kmm

/**
 * The BasicAuthenticator class is an authenticator that will authenticate using HTTP Basic
 * auth with the given username and password. This should only be used over an SSL/TLS connection,
 * as otherwise it's very easy for anyone sniffing network traffic to read the password.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
public expect class BasicAuthenticator

/**
 * Create a Basic Authenticator.
 * The new instance contains a copy of the password char[] parameter:
 * the owner of the original retains the responsibility for zeroing it before releasing it.
 */
constructor(username: String, password: CharArray) : Authenticator {

    public val username: String

    /**
     * Get the password.
     * The returned char[] is a copy: the owner is responsible for zeroing it before releasing it.
     *
     * @return the password, as a char[].
     */
    public val passwordChars: CharArray
}
