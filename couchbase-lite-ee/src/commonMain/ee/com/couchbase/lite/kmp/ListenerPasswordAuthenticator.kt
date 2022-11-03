package com.couchbase.lite.kmp

/**
 * Authenticator for HTTP Listener password authentication
 */
public expect class ListenerPasswordAuthenticator

/**
 * Create an Authenticator using the passed delegate.
 * See [ListenerPasswordAuthenticatorDelegate]
 *
 * @param delegate where the action is.
 */
constructor(delegate: ListenerPasswordAuthenticatorDelegate) : ListenerAuthenticator
