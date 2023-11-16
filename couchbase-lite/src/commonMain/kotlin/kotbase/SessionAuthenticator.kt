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

/**
 * SessionAuthenticator class is an authenticator that will authenticate by using the session ID of
 * the session created by a Sync Gateway
 */
public expect class SessionAuthenticator : Authenticator {

    /**
     * Initializes with the Sync Gateway session ID and uses the default cookie name.
     *
     * @param sessionID Sync Gateway session ID
     */
    public constructor(sessionID: String)

    /**
     * Initializes with the session ID and the cookie name. If the given cookieName
     * is null, the default cookie name will be used.
     *
     * @param sessionID  Sync Gateway session ID
     * @param cookieName The cookie name
     */
    public constructor(sessionID: String, cookieName: String?)

    /**
     * Session ID of the session created by a Sync Gateway.
     */
    public val sessionID: String

    /**
     * Session cookie name that the session ID value will be set to when communicating
     * the Sync Gateway.
     */
    public val cookieName: String
}
