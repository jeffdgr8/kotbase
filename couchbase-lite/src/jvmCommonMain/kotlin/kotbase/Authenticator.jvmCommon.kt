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

import com.couchbase.lite.Authenticator as CBLAuthenticator

internal actual class AuthenticatorPlatformState(
    internal val actual: CBLAuthenticator
)

public actual sealed class Authenticator(actual: CBLAuthenticator) {

    internal actual val platformState = AuthenticatorPlatformState(actual)

    override fun equals(other: Any?): Boolean =
        actual == (other as? Authenticator)?.actual

    override fun hashCode(): Int =
        actual.hashCode()

    override fun toString(): String =
        actual.toString()
}

internal val Authenticator.actual: CBLAuthenticator
    get() = platformState.actual