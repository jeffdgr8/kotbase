package com.couchbase.lite.kmp

/**
 * Functional Interface for an Authenticator that uses an authentication strategy based on client supplied certificates.
 * Pass implementations of this interface to the [ListenerCertificateAuthenticator] to realize
 * specific authentication strategies.
 *
 * Authenticate a client based on the passed certificates.
 * Note that the passed certificates have not been validated. All validation and authorization
 * are the responsibility of the implementation.
 *
 * @param certs client supplied certificates.
 * @return true to validate the client.
 */
public typealias ListenerCertificateAuthenticatorDelegate = (certs: List<ByteArray>) -> Boolean
