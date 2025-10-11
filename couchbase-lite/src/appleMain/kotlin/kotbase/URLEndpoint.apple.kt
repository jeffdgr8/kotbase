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

import cocoapods.CouchbaseLite.CBLURLEndpoint
import platform.Foundation.NSURL

public actual class URLEndpoint
internal constructor(override val actual: CBLURLEndpoint) : Endpoint(actual) {

    public actual constructor(url: String) : this(CBLURLEndpoint(validate(url)))

    public actual val url: String
        get() = actual.url.path!!

    private companion object {

        private const val SCHEME_STD = "ws"
        private const val SCHEME_TLS = "wss"

        private fun validate(url: String): NSURL {
            val nsUrl = requireNotNull(NSURL.URLWithString(url)) { "Invalid URLEndpoint url ($url)" }

            val scheme = nsUrl.scheme
            require((SCHEME_STD == scheme) || (SCHEME_TLS == scheme)) {
                "Invalid scheme for URLEndpoint url ($url). It must be either 'ws:' or 'wss:'."
            }

            require(nsUrl.user == null && nsUrl.password == null) {
                "Embedded credentials in a URL (username:password@url) are not allowed. Use the BasicAuthenticator class instead."
            }

            return nsUrl
        }
    }
}
