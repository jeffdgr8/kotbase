package com.couchbase.lite.kmm

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticatorTest {

    @Test
    fun testBasicAuthenticatorInstance() {
        val username = "someUsername"
        val password = "somePassword"
        val auth = BasicAuthenticator(username, password.toCharArray())
        assertEquals(username, auth.username)
        assertEquals(password, auth.passwordChars.concatToString())
    }

    @Test
    fun testSessionAuthenticatorWithSessionID() {
        val sessionID = "someSessionID"
        val auth = SessionAuthenticator(sessionID)
        assertEquals(sessionID, auth.sessionID)
        assertEquals("SyncGatewaySession", auth.cookieName)
    }

    @Test
    fun testSessionAuthenticatorWithSessionIDAndCookie() {
        val sessionID = "someSessionID"
        val cookie = "someCookie"
        val auth = SessionAuthenticator(sessionID, cookie)
        assertEquals(sessionID, auth.sessionID)
        assertEquals(cookie, auth.cookieName)
    }

    @Test
    fun testSessionAuthenticatorEmptyCookie() {
        val sessionID = "someSessionID"
        val auth = SessionAuthenticator(sessionID, null)
        assertEquals(sessionID, auth.sessionID)
        assertEquals("SyncGatewaySession", auth.cookieName)
    }
}
