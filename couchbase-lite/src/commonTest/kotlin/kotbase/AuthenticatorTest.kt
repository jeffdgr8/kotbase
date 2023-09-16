/*
 * Copyright 2022-2023 Jeff Lockhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kotbase

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
