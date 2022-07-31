package com.couchbase.lite.kmm

/**
 * Authenticator is an opaque authenticator interface and not intended for application to
 * implement a custom authenticator by subclassing Authenticator interface.
 *
 * NOTE: Authenticator is an abstract class (instead of an interface) so that
 * the `authenticate` method is visible only in this package.
 */
public expect abstract class Authenticator
