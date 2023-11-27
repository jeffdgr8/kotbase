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

import com.couchbase.lite.SessionAuthenticator as CBLSessionAuthenticator

public actual class SessionAuthenticator
internal constructor(
    override val actual: CBLSessionAuthenticator
) : Authenticator(actual) {

    public actual constructor(sessionID: String) : this(CBLSessionAuthenticator(sessionID))

    public actual constructor(sessionID: String, cookieName: String?) : this(
        CBLSessionAuthenticator(sessionID, cookieName)
    )

    public actual val sessionID: String
        get() = actual.sessionID

    public actual val cookieName: String
        get() = actual.cookieName!!
}
